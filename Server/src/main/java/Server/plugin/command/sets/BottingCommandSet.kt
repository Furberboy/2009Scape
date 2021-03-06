package plugin.command.sets

import core.game.component.Component
import core.plugin.InitializablePlugin
import plugin.ai.general.GeneralBotCreator
import plugin.ai.general.scriptrepository.PlayerScripts
import plugin.ai.general.scriptrepository.Script
import plugin.command.Command
import plugin.command.CommandMapping
import plugin.command.CommandSet
import plugin.stringtools.colorize

@InitializablePlugin
class BottingCommandSet : CommandSet(Command.Privilege.STANDARD) {
    override fun defineCommands() {
        define("scripts"){player, _ ->
            if(!player.getAttribute("botting:warning_shown",false)){
                player.dialogueInterpreter.sendDialogue(colorize("%RWARNING: Running a bot script will permanently remove you"),colorize("%Rfrom the highscores."))
                player.dialogueInterpreter.addAction { player, buttonId -> player.setAttribute("/save:botting:warning_shown",true) }
                return@define
            }
            for (i in 0..310) {
                player.packetDispatch.sendString("", 275, i)
            }
            var lineid = 11
            player.packetDispatch.sendString("Bot Scripts",275,2)
            for(script in PlayerScripts.identifierMap.values) {
                player.packetDispatch.sendString("<bold>${script.name}</bold>", 275, lineid++)
                script.description.forEach { line ->
                    player.packetDispatch.sendString(line,275,lineid++)
                }
                player.packetDispatch.sendString("<img=3> ::script ${script.identifier}",275,lineid++)
                player.packetDispatch.sendString("<str>                                 </str>",275,lineid++)
            }
            player.interfaceManager.open(Component(275))
        }
        define("script"){player,args ->
            if(!player.getAttribute("botting:warning_shown",false)){
                player.dialogueInterpreter.sendDialogue(colorize("%RWARNING: Running a bot script will permanently remove you from the highscores."))
                player.dialogueInterpreter.addAction { player, buttonId -> player.setAttribute("/save:botting:warning_shown",true) }
                return@define
            }
            if(args.size < 2){
                reject(player,"Usage: ::script identifier")
                return@define
            }
            val identifier = args[1]
            val script = PlayerScripts.identifierMap[identifier]
            if(script == null){
                reject(player,"Invalid script identifier")
                return@define
            }
            player.interfaceManager.close()
            GeneralBotCreator(script.clazz.newInstance() as Script,player,true)
            player.sendMessage(colorize("%RStarting script..."))
            player.sendMessage(colorize("%RTo stop the script, do ::stopscript or log out."))

        }
        define("stopscript"){player,args ->
            val pulse: GeneralBotCreator.BotScriptPulse? = player.getAttribute("botting:script",null)
            pulse?.stop()
            player.interfaceManager.closeOverlay()
        }
    }
}