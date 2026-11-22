package com.voicebubble.desktop

import com.voicebubble.Adapt
import com.voicebubble.Ai
import com.voicebubble.AppDetector
import com.voicebubble.Audio
import com.voicebubble.Characters
import com.voicebubble.Commands
import com.voicebubble.Features
import com.voicebubble.Instant
import com.voicebubble.Limit
import com.voicebubble.Models
import com.voicebubble.RobloxSafe
import com.voicebubble.Royal
import com.voicebubble.Stats
import com.voicebubble.Styles
import com.voicebubble.TextTools
import com.voicebubble.Whisper
import com.voicebubble.Words
import com.voicebubble.clean
import com.voicebubble.letterCount
import java.util.concurrent.Executors
import kotlin.math.sqrt

/**
 * What happens when you talk, the same steps as on the phone:
 * voice commands → emoji → fillers → numbers & dates → style per app → dictionary & word packs → shortcuts
 * → AI fixes mistakes → character → translation → Roblox-safe words → length limit → typed into the program.
 */
object Engine {
    enum class State { IDLE, LISTENING, WORKING }

    /** The bubble and the little messages next to it. */
    var onState: (State) -> Unit = {}
    var onToast: (String) -> Unit = {}
    var onReplies: (List<String>, String) -> Unit = { _, _ -> }

    @Volatile var state = State.IDLE; private set
    private val mic = Mic()
    val level get() = mic.level
    val seconds get() = mic.seconds
    private val worker = Executors.newSingleThreadExecutor { Thread(it, "voice-work").apply { isDaemon = true } }
    private val prefs get() = App.prefs
    private val ctx get() = App

    private fun set(s: State) { state = s; onState(s) }
    private fun toast(msg: String) = onToast(msg)

    fun toggle() = if (state == State.LISTENING) stop() else if (state == State.IDLE) start() else Unit

    fun start() {
        if (state != State.IDLE) return
        val model = Models.byId(prefs.modelId)
        if (!Models.isDownloaded(ctx, model)) {
            toast("Download a speech model first (Settings → Speech)")
            Settings.open(tab = 0)
            return
        }
        try { mic.start() } catch (e: Exception) {
            toast("Couldn't use the microphone: ${e.message ?: "none found"}. Check Windows Settings → Privacy → Microphone.")
            return
        }
        Sound.start()
        set(State.LISTENING)
        // Load the model meanwhile, so it's ready when you stop.
        worker.execute { runCatching { Whisper.ensureLoaded(Models.file(ctx, model).path) } }
    }

    fun cancel() {
        if (state == State.LISTENING) { mic.stop(); set(State.IDLE); toast("Cancelled") }
        else if (state == State.WORKING) { Whisper.abort(); cancelled = true }
    }
    @Volatile private var cancelled = false

    fun stop() {
        if (state != State.LISTENING) return
        val audio = mic.stop()
        Sound.stop()
        if (!hasSpeech(audio)) { set(State.IDLE); toast("Didn't hear anything"); return }
        cancelled = false
        set(State.WORKING)
        worker.execute {
            try { finish(audio) } catch (e: Throwable) { toast("Something went wrong: ${e.message}") }
            finally { set(State.IDLE) }
        }
    }

    // ---------------- speech to text ----------------

    private class Session(val app: String?, val style: Styles.Style, val cat: Styles.Category, val limit: Int?,
                          val lang: String, val modelPath: String, val toEnglish: Boolean, val aiTarget: String?, val prompt: String)

    private fun session(): Session {
        var chosen = Models.byId(prefs.modelId)
        val app = AppDetector.current(ctx)
        val appLang = Styles.langFor(ctx, app)
        var lang = appLang ?: if (chosen.englishOnly) "en" else prefs.lang
        if (lang != "en" && chosen.englishOnly) {
            val alt = Models.all.firstOrNull { !it.englishOnly && Models.isDownloaded(ctx, it) }
            if (alt != null) chosen = alt else { toast("Download an \"all languages\" model to dictate in that language"); lang = "en" }
        }
        val target = prefs.translateTo
        val toEnglish = target.equals("English", true) && !chosen.englishOnly
        val aiTarget = target?.takeIf { !toEnglish && !(it.equals("English", true) && !Ai.hasKey(ctx)) }
        return Session(app, Styles.forApp(ctx, app), Styles.categoryOf(app), Limit.forApp(ctx, app), lang,
            Models.file(ctx, chosen).path, toEnglish, aiTarget,
            listOf(prefs.commandWord + ".", Words.prompt(ctx), Adapt.prompt(ctx)).filter { it.isNotBlank() }.joinToString(" "))
    }

    /** Long recordings are read in pieces of up to ~28 s, cut in a pause. */
    private fun pieces(a: FloatArray): List<FloatArray> {
        val out = ArrayList<FloatArray>()
        var start = 0
        val max = Mic.RATE * 28; val min = Mic.RATE * 18
        while (a.size - start > max) {
            val rms = Audio.frameRms(a.copyOfRange(start + min, start + max))
            val q = rms.indices.minByOrNull { rms[it] } ?: 0
            val cut = start + min + q * Audio.FRAME + Audio.FRAME / 2
            out += a.copyOfRange(start, cut); start = cut
        }
        out += a.copyOfRange(start, a.size)
        return out
    }

    private fun finish(audio: FloatArray) {
        val s = session()
        val seconds = audio.size / Mic.RATE.toFloat()
        val parts = ArrayList<String>()
        Whisper.threads = (Runtime.getRuntime().availableProcessors() / 2).coerceIn(2, 8)
        for (p in pieces(audio)) {
            if (cancelled) { toast("Cancelled"); return }
            val trimmed = Audio.trimSilence(p)
            if (!hasSpeech(trimmed)) continue
            val prev = parts.lastOrNull().orEmpty().takeLast(200)
            val txt = Whisper.transcribe(s.modelPath, trimmed, s.lang, (s.prompt + " " + prev).trim(), prefs.extraAccuracy, s.toEnglish)
                ?: run { if (!cancelled) toast("Couldn't turn that into text"); return }
            clean(txt).takeIf { it.isNotBlank() }?.let { parts += it }
        }
        if (cancelled) { toast("Cancelled"); return }
        val said = parts.joinToString(" ").trim().takeIf { it.isNotEmpty() && letterCount(it) > 0 }
            ?: run { toast("Didn't catch any words"); return }
        Adapt.observe(ctx, said)

        val split = if (Commands.isScratch(said)) Commands.Split("", Commands.Cmd.Undo) else Commands.split(said, prefs.commandWord)
        if (split != null) {
            val before = split.before.takeIf { it.isNotEmpty() }?.let { process(it, s.style, s.cat) }
            val cmd = split.cmd
            when {
                before == null -> runCommand(cmd)
                cmd is Commands.Cmd.Rewrite -> rewrite(cmd.instruction, before, s.limit)
                else -> { deliver(finishForApp(s.app, before, s.limit)); Stats.record(ctx, before, seconds, s.app); runCommand(cmd) }
            }
            return
        }

        var text = process(said, s.style, s.cat)
        var problem: String? = null
        val persona = prefs.persona
        if (prefs.aiFix && persona == null && s.aiTarget == null && Ai.hasKey(ctx) && text.split(" ").size >= 3)
            text = runCatching { Ai.fixMistakes(ctx, text, Words.prompt(ctx).trimEnd('.')) }.getOrDefault(text)
        if (persona != null) {
            if (Royal.wanted(persona) && !Characters.isMix(persona) && (prefs.instantRoyal || !Ai.hasKey(ctx))) {
                text = Royal.apply(text, prefs.charIntensity)
            } else if (Instant.has(persona) && !Characters.isMix(persona) && (prefs.instantChars || !Ai.hasKey(ctx))) {
                text = Instant.apply(persona, text, prefs.charIntensity) ?: text
            } else if (Ai.hasKey(ctx)) {
                text = try { Ai.rewrite(ctx, text, Royal.instruction(persona, ctx), s.limit, who = persona) } catch (e: Ai.Failure) {
                    // Never random text: the instant character if there is one, otherwise your own words.
                    val instant = if (!Characters.isMix(persona)) Instant.apply(persona, text, prefs.charIntensity) else null
                    problem = if (instant != null) "AI character didn't work this time, used the instant one" else e.message
                    instant ?: text
                }
            }
        }
        if (s.aiTarget != null)
            text = try { Ai.translate(ctx, text, s.aiTarget, s.limit) } catch (e: Ai.Failure) { problem = e.message; text }
        if (prefs.capsLock) text = text.uppercase()
        text = finishForApp(s.app, text, s.limit)
        if (cancelled) { toast("Cancelled"); return }
        problem?.let { toast("AI step skipped: $it") }
        deliver(text)
        Stats.record(ctx, text, seconds, s.app)
    }

    private fun process(said: String, style: Styles.Style, cat: Styles.Category): String {
        var t = said
        if (Features.isOn(ctx, Commands.Feature.COMMANDS)) t = TextTools.voiceCommands(t)
        if (Features.isOn(ctx, Commands.Feature.EMOJI)) t = TextTools.emoji(t)
        if (Features.isOn(ctx, Commands.Feature.FILLER)) t = TextTools.removeFillers(t)
        if (Features.isOn(ctx, Commands.Feature.NUMBERS)) t = TextTools.numbersAndDates(t)
        t = Styles.apply(t, style, cat)
        return Words.apply(ctx, t, capitalize = style != Styles.Style.VERY_CASUAL && style != Styles.Style.GEN_Z)
    }

    private fun finishForApp(app: String?, text: String, limit: Int?): String {
        var t = runCatching { Words.applyDictionary(text, Words.dictionary(ctx), Words.shortcuts(ctx)) }.getOrDefault(text)
        if (app == Limit.ROBLOX && prefs.robloxSafe) {
            val r = RobloxSafe.clean(t)
            if (r.changed > 0) toast("Made it Roblox-safe: ${r.changed} word${if (r.changed == 1) "" else "s"} changed")
            t = r.text
        }
        if (limit != null && t.length > limit) {
            if (Ai.hasKey(ctx)) runCatching { Ai.shorten(ctx, t, limit) }.getOrNull()
                ?.takeIf { it.isNotBlank() && it.length <= limit }?.let { t = it }
            if (t.length > limit) t = Limit.squeeze(t, limit)
            if (app == Limit.ROBLOX && prefs.robloxSafe) t = RobloxSafe.clean(t).text
        }
        return t
    }

    /** For checking from the command line: "VoiceBubble --transcribe file.wav" prints instead of typing. */
    var dryRun: ((String) -> Unit)? = null

    fun transcribeFile(path: String) {
        val ais = javax.sound.sampled.AudioSystem.getAudioInputStream(java.io.File(path))
        val bytes = ais.readAllBytes()
        val audio = FloatArray(bytes.size / 2) { i -> ((bytes[2 * i + 1].toInt() shl 8) or (bytes[2 * i].toInt() and 0xFF)).toShort() / 32768f }
        finish(audio)
    }

    private fun deliver(text: String) {
        if (text.isBlank()) return
        dryRun?.let { it(text); return }
        // A space before it when you're carrying on after something you dictated.
        Typer.type(text)
    }

    // ---------------- "out of context, …" ----------------

    private fun runCommand(cmd: Commands.Cmd) {
        when (cmd) {
            is Commands.Cmd.Toggle -> { Features.set(ctx, cmd.feature, cmd.on); toast("${cmd.feature.title} ${if (cmd.on) "on ✓" else "off"}") }
            is Commands.Cmd.Translate -> {
                val lang = cmd.language?.split(' ')?.joinToString(" ") { w -> w.replaceFirstChar { it.uppercaseChar() } }
                prefs.translateTo = lang
                toast(when {
                    lang == null -> "Translation off"
                    lang.equals("English", true) -> "Translating everything into English"
                    !Ai.hasKey(ctx) -> "Translating into $lang needs an AI key (Settings → AI)"
                    else -> "Translating everything into $lang"
                })
            }
            Commands.Cmd.Undo -> toast(if (Typer.undo()) "Undone" else "Nothing to undo")
            is Commands.Cmd.Rewrite -> rewrite(cmd.instruction, null, Limit.forApp(ctx, AppDetector.current(ctx)))
            is Commands.Cmd.Reply -> reply(cmd.instruction)
            is Commands.Cmd.Summarize -> summarize(cmd.instruction)
            is Commands.Cmd.RoyalMode -> {
                prefs.persona = Royal.NAME
                cmd.instant?.let { prefs.instantRoyal = it }
                toast(if (prefs.instantRoyal || !Ai.hasKey(ctx)) "Royal English on (instant)" else "AI Royal English on")
            }
            is Commands.Cmd.Persona -> {
                prefs.persona = cmd.who
                toast(when {
                    cmd.who == null -> "Back to your normal voice"
                    !Ai.hasKey(ctx) && !Instant.has(cmd.who) -> "Talking like ${cmd.who} needs an AI key (Settings → AI)"
                    else -> "Talking like ${cmd.who} until you say \"${prefs.commandWord}, talk normally\""
                })
            }
            is Commands.Cmd.Length -> { prefs.aiLength = cmd.length; toast("AI text: ${Ai.lengthName(cmd.length).lowercase()}") }
            is Commands.Cmd.Unknown -> toast("Didn't get that command: \"${cmd.said.take(40)}\"")
        }
        Bubble.refresh()
    }

    private fun swapIn(out: String, fresh: Boolean) {
        if (fresh) deliver(out) else if (!Typer.replaceLast(out)) { Typer.copy(out); toast("Copied: paste it over the old one") }
    }

    fun rewrite(instruction: String, fresh: String?, limit: Int?) {
        val text = fresh ?: Typer.last ?: run { toast("Dictate something first, then ask to rewrite it"); return }
        val who = Regex("(?i)^make me sound like\\s+(.+)$").find(instruction.trim())?.groupValues?.get(1)
        if (Royal.wanted(instruction) && (prefs.instantRoyal || !Ai.hasKey(ctx))) { swapIn(Royal.apply(text, prefs.charIntensity), fresh != null); return }
        if (who != null && Instant.has(who) && !Characters.isMix(who) && (prefs.instantChars || !Ai.hasKey(ctx))) {
            swapIn(Instant.apply(who, text, prefs.charIntensity) ?: text, fresh != null); return
        }
        if (!Ai.hasKey(ctx)) { toast("Rewriting needs an AI key (Settings → AI)"); fresh?.let { deliver(it) }; return }
        val instr = when {
            who != null && (Characters.isMix(who) || Characters.guideFor(who, ctx) != null) -> Characters.instruction(who, ctx)
            Royal.wanted(instruction) -> Royal.AI
            else -> instruction
        }
        try { swapIn(Ai.rewrite(ctx, text, instr, limit, who = who), fresh != null) }
        catch (e: Ai.Failure) { toast("AI: ${e.message}"); fresh?.let { deliver(it) } }
    }

    /** "reply": select the message(s) you got, then say it. Three replies to click. */
    private fun reply(instruction: String) {
        if (!Ai.hasKey(ctx)) { toast("Replies need an AI key (Settings → AI)"); return }
        val chat = Typer.copySelection() ?: run { toast("Select the message you're answering first, then say \"reply\""); return }
        try { onReplies(Ai.replies(ctx, chat, instruction), "Click a reply to type it") } catch (e: Ai.Failure) { toast("AI: ${e.message}") }
    }

    private fun summarize(instruction: String) {
        if (!Ai.hasKey(ctx)) { toast("Summaries need an AI key (Settings → AI)"); return }
        val text = Typer.copySelection() ?: run { toast("Select the text to summarise first"); return }
        try { onReplies(listOf(Ai.summary(ctx, text, instruction)), "Summary (click to type it)") } catch (e: Ai.Failure) { toast("AI: ${e.message}") }
    }

    /** Picked a reply: type it. */
    fun typeReply(text: String) = worker.execute { deliver(text) }

    fun hasSpeech(audio: FloatArray): Boolean {
        if (audio.size < Mic.RATE / 3) return false
        val win = Mic.RATE / 10
        var best = 0.0
        var i = 0
        while (i + win <= audio.size) {
            var s = 0.0
            for (j in i until i + win) s += audio[j] * audio[j]
            best = maxOf(best, s / win); i += win
        }
        return sqrt(best) > 0.01
    }
}
