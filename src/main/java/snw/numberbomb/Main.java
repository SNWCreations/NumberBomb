package snw.numberbomb;

import snw.jkook.JKook;
import snw.jkook.command.JKookCommand;
import snw.jkook.message.TextChannelMessage;
import snw.jkook.message.component.TextComponent;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.plugin.BasePlugin;

public class Main extends BasePlugin {
    private static Main INSTANCE;
    private static final MultipleCardComponent COMMAND_HELP_CARD;
    private SessionStorage storage;


    static {
        COMMAND_HELP_CARD = Session.drawCard(
                "帮助:\n" +
                        "`/num play` - 启动一场游戏\n" +
                        "`/num answer <数字>` - 提交你的回答\n" +
                        "`/num exit` - 退出游戏进程"
        );
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        storage = new SessionStorage();

        new JKookCommand("num")
                .setDescription("数字炸弹小游戏根命令")
                .executesUser(
                        (sender, arguments, message) -> {
                            if (message instanceof TextChannelMessage) {
                                ((TextChannelMessage) message).getChannel().sendComponent(
                                        COMMAND_HELP_CARD, null, null
                                );
                            }
                        }
                )
                .addSubcommand(
                        new JKookCommand("play")
                                .executesUser(
                                        (sender, arguments, message) -> {
                                            if (message instanceof TextChannelMessage) {
                                                if (storage.getSession(sender) == null) {
                                                    Session session = storage.createSession(sender);
                                                    String s = ((TextChannelMessage) message).getChannel().sendComponent(
                                                            session.drawCard(), null, null
                                                    );
                                                    session.setMessage(JKook.getCore().getUnsafe().getTextChannelMessage(s));
                                                    session.setChannel(((TextChannelMessage) message).getChannel());
                                                } else {
                                                    ((TextChannelMessage) message).replyTemp(new TextComponent("你已经在游戏了。"));
                                                }
                                            }
                                        }
                                )
                )
                .addSubcommand(
                        new JKookCommand("answer")
                                .executesUser(
                                        (sender, arguments, message) -> {
                                            if (message instanceof TextChannelMessage) {
                                                if (arguments.length > 0) {
                                                    Session session = storage.getSession(sender);
                                                    if (session != null) {
                                                        int answer;
                                                        try {
                                                            answer = Integer.parseInt(arguments[0]);
                                                        } catch (NumberFormatException e) {
                                                            ((TextChannelMessage) message).replyTemp(new TextComponent("无效的参数 - 不是数字。"));
                                                            message.delete();
                                                            return;
                                                        }
                                                        session.execute(answer);
                                                        message.delete();
                                                    } else {
                                                        ((TextChannelMessage) message).replyTemp(new TextComponent("你未在游戏。"));
                                                    }
                                                } else {
                                                    ((TextChannelMessage) message).replyTemp(new TextComponent("参数不足。"));
                                                }
                                            }
                                        }
                                )
                )
                .addSubcommand(
                        new JKookCommand("rule")
                                .executesUser(
                                        (sender, arguments, message) -> {
                                            if (message != null) {
                                                message.reply(Session.HELP_CARD);
                                            } else {
                                                sender.sendPrivateMessage(Session.HELP_CARD);
                                            }
                                        }
                                )
                )
                .addSubcommand(
                        new JKookCommand("exit")
                                .executesUser(
                                        (sender, arguments, message) -> {
                                            if (message instanceof TextChannelMessage) {
                                                if (storage.destorySession(sender)) {
                                                    ((TextChannelMessage) message).reply(new TextComponent("操作成功。"));
                                                } else {
                                                    ((TextChannelMessage) message).reply(new TextComponent("你并没有在游戏。"));
                                                }
                                            }
                                        }
                                )
                )
                .register();
    }

    public static Main getInstance() {
        return INSTANCE;
    }
}
