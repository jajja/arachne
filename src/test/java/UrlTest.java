import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.jajja.arachne.exceptions.MalformedUriException;
import com.jajja.arachne.net.Url;

public class UrlTest {
    
    @Test public void resolve() {
        String baseUrl = "http://a/b/c/d;p?q#f";

        LinkedHashMap<String, String> tests = new LinkedHashMap<String, String>();
        /*tests.put("g:h", "g:h");*/        // fails, result: g://h -- web browsers get it "wrong" too.
        tests.put("g", "http://a/b/c/g");
        tests.put("./g", "http://a/b/c/g");
        tests.put("g/", "http://a/b/c/g/");
        tests.put("/g", "http://a/g");
        tests.put("//g", "http://g");
        tests.put("?y", "http://a/b/c/d;p?y");
        tests.put("g?y", "http://a/b/c/g?y");
        tests.put("g?y/./x", "http://a/b/c/g?y/./x");
        tests.put("#s", "http://a/b/c/d;p?q#s");
        tests.put("g#s", "http://a/b/c/g#s");
        tests.put("g#s/./x", "http://a/b/c/g#s/./x");
        tests.put("g?y#s", "http://a/b/c/g?y#s");
        tests.put(";x", "http://a/b/c/d;x");            // XXX fails
        tests.put("g;x", "http://a/b/c/g;x");
        tests.put("g;x?y#s", "http://a/b/c/g;x?y#s");
        tests.put(".", "http://a/b/c/");
        tests.put("./", "http://a/b/c/");
        tests.put("..", "http://a/b/");
        tests.put("../", "http://a/b/");
        tests.put("../g", "http://a/b/g");
        tests.put("../..", "http://a/");
        tests.put("../../", "http://a/");
        tests.put("../../g", "http://a/g");
        // abnormal tests
        tests.put("", "http://a/b/c/d;p?q#f");
        //tests.put("../../../g", "http://a/../g");         // fails - mimicking web browsers
        //tests.put("../../../../g", "http://a/../../g");   // fails - mimicking web browsers
        //tests.put("/./g", "http://a/./g");                // fails - mimicking web browsers
        //tests.put("/../g", "http://a/../g");              // fails - mimicking web browsers
        tests.put("g.", "http://a/b/c/g.");
        tests.put(".g", "http://a/b/c/.g");
        tests.put("g..", "http://a/b/c/g..");
        tests.put("..g", "http://a/b/c/..g");
        tests.put("./../g", "http://a/b/g");
        tests.put("./g/.", "http://a/b/c/g/");
        tests.put("g/./h", "http://a/b/c/g/h");
        tests.put("g/../h", "http://a/b/c/h");
        // "should be avoided by future parsers"
        //tests.put("http:g", "http:g");                    // fails
        //tests.put("http", "http:");                       // fails


        for (Entry<String, String> test : tests.entrySet()) {
            String relPath = test.getKey();
            String expected = test.getValue();
            try {
                Url u = new Url(baseUrl);
                u = u.resolve(relPath);
                System.out.println((u.toString().equals(expected) ? "PASS " : "FAIL ") +
                        "   " + baseUrl + " + " + relPath + " = " + u.toString() + "   should equal " + expected);
                Assert.assertEquals(expected, u.toString());
            } catch (MalformedUriException e) {
                e.printStackTrace();
            }
        }
        
    }
    
}
