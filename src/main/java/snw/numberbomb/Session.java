package snw.numberbomb;

import snw.jkook.entity.User;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.message.TextChannelMessage;
import snw.jkook.message.component.TextComponent;
import snw.jkook.message.component.card.CardBuilder;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.message.component.card.Size;
import snw.jkook.message.component.card.Theme;
import snw.jkook.message.component.card.element.MarkdownElement;
import snw.jkook.message.component.card.element.PlainTextElement;
import snw.jkook.message.component.card.module.ContextModule;
import snw.jkook.message.component.card.module.DividerModule;
import snw.jkook.message.component.card.module.HeaderModule;
import snw.jkook.message.component.card.module.SectionModule;
import snw.jkook.util.Validate;

import java.util.Collections;
import java.util.Random;

public class Session {
    public static final MultipleCardComponent HELP_CARD;
    private final SessionStorage parent;
    private final User user;
    private int min;
    private int max;
    private int target;
    private TextChannelMessage message;
    private TextChannel channel;

    static {
        HELP_CARD = drawCard(
                "规则:\n" +
                        "在一个数字范围内，有一个数字是炸弹，\n" +
                        "猜中这个炸弹即为失败，反之逐步缩小范围，\n" +
                        "若最后两个边界值将炸弹数字夹在中间，则胜利。\n" +
                        "---\n" +
                        "如范围是 1~100，炸弹数字是 60，猜了一个数字 30。\n" +
                        "因为 30 不是炸弹，且小于炸弹数字 60，所以现在猜数字的范围就缩小到 30~100。\n" +
                        "又猜了一个数字 80，80 也不是炸弹，又因为其大于炸弹数字 60，所以范围缩小到 30~80 。\n" +
                        "最后猜了 60，正好等于炸弹数字，失败。\n" +
                        "不可以输入边界值以外的数字**（包括边界值本身）**。"
        );
    }

    public Session(User user, SessionStorage storage) {
        this(user, Main.getInstance().getConfig().getInt("min", 0), Main.getInstance().getConfig().getInt("max", 100), storage);
    }

    public Session(User user, int min, int max, SessionStorage storage) {
        Validate.isTrue(min < max, "Minimum number should less than maximum number");
        Validate.isFalse(Math.abs(max - min) == 2, "Always win?"); // if max - min == 2, the player won
        this.user = user;
        this.min = min;
        this.max = max;
        this.parent = storage;
        do {
            this.target = (new Random().nextInt(max - min) + min);
        } while (this.target <= this.min); // make sure the target is in the range (excluding minimum and maximum)
    }

    public User getUser() {
        return user;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        Validate.isTrue(min < max, "Minimum number out of range");
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        Validate.isTrue(max > min, "Maximum number out of range");
        this.max = max;
    }

    public TextChannelMessage getMessage() {
        return message;
    }

    public void setMessage(TextChannelMessage message) {
        Validate.isFalse(message == null, "This session has already bound to a message.");
        this.message = message;
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public void execute(int answer) {
        if (answer <= min || answer >= max) {
            channel.sendComponent(
                    new TextComponent("无效的参数 - 超出有效范围。"), null, user
            );
            return;
        }
        if (answer != target) {
            if (answer > target) {
                max = answer;
            } else {
                min = answer;
            }
            if (Math.abs(this.max - this.min) == 2) {
                message.setComponent(
                        drawCard("你赢了！")
                );
            } else {
                message.setComponent(drawCard());
            }
        } else {
            message.setComponent(
                    drawCard("你输了！炸弹数字: " + target)
            );
            parent.destorySession(user);
        }
    }

    public MultipleCardComponent drawCard() {
        return drawCard("请在 " + min + " 和 " + max + " 之间选择一个数字。\n命令格式：`/num answer <目标数字>`");
    }

    public static MultipleCardComponent drawCard(String infoMsg) {
        return new CardBuilder()
                .setTheme(Theme.DANGER)
                .setSize(Size.LG)
                .addModule(
                        new HeaderModule("数字炸弹")
                )
                .addModule(DividerModule.INSTANCE)
                .addModule(
                        new SectionModule(new MarkdownElement(infoMsg), null, null)
                )
                .addModule(DividerModule.INSTANCE)
                .addModule(
                        new ContextModule(Collections.singletonList(new PlainTextElement("由 ZX夏夜之风 开发")))
                )
                .build();
    }
}
