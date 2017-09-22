import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonTest {

    @Test
    public void test() {
        String pattern = "(\\s+||,|;)IN\\[[a-zA-Z0-9,]+\\]";

        Assert.assertTrue(haveExpression("This ia a test IN[11,22]", pattern));

        Assert.assertTrue(haveExpression("IN[11,22]", pattern));

        Assert.assertTrue(haveExpression(",IN[11,22]", pattern));

        Assert.assertTrue(haveExpression("This ia a test IN[11,22]aabb", pattern));

        Assert.assertFalse(haveExpression("This ia a test IN[11,22,##]aabb", pattern));

    }

    @Test
    public void testSplit() {
        String delimiter = "\\s+";

        Assert.assertEquals("111", "111 22 33".split(delimiter)[0]);
        Assert.assertEquals("111", "111".split(delimiter)[0]);
    }

    @Test
    public void testPattern() {
        String content = "1111111111,111";
        String regEx = "(\\d{1,11}[,]?)+";

        Pattern  pattern  =  Pattern.compile(regEx);
        //  忽略大小写的写法
        //  Pattern  pat  =  Pattern.compile(regEx,  Pattern.CASE_INSENSITIVE);

        Matcher  matcher  =  pattern.matcher(content);
        //  字符串是否与正则表达式相匹配
        Assert.assertTrue(matcher.matches());

        content = "   DF{yyyy/MM/dd HH:mm:ss}";
        regEx = "(\\s+||,|;)DF\\{[yYmMdDhHSsWw:\\s0-9\\-/]+}";
        pattern  =  Pattern.compile(regEx);
        matcher  =  pattern.matcher(content);
        Assert.assertTrue(matcher.matches());

        String result = extractMessage(content, '{', '}').get(0);
        System.out.println(result);
        Assert.assertEquals("yyyy/MM/dd HH:mm:ss", result);

        content = "$NOWHITESPACE";
        regEx = "(\\s|)\\$NOWHITESPACE";
        pattern  =  Pattern.compile(regEx);
        matcher  =  pattern.matcher(content);
        Assert.assertTrue(matcher.matches());
    }

    public boolean haveExpression(String content, String reg){
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while(m.find()){
            return true;
        }
        return false;
    }

    public List<String> extractMessage(String msg, char startChar, char endChar) {
        List<String> list = new ArrayList<>();
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == startChar) {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (msg.charAt(i) == endChar) {
                endFlag++;
                if (endFlag == startFlag) {
                    list.add(msg.substring(start + 1, i));
                }
            }
        }
        return list;
    }
}
