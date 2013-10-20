import org.junit.Assert;
import org.junit.Test;

import com.jajja.arachne.exceptions.MalformedDomainException;
import com.jajja.arachne.net.Domain;
import com.jajja.arachne.net.Record;

public class DomainTest {

    @Test public void mixcase() { // Mixed case.
        checkPublicSuffix("COM", null);
        checkPublicSuffix("example.COM", "example.com");
        checkPublicSuffix("WwW.example.COM", "example.com");
    }

    @Test public void period() { // Leading dot.
        checkPublicSuffix(".com", null);
        checkPublicSuffix(".example", null);
        checkPublicSuffix(".example.com", null);
        checkPublicSuffix(".example.example", null);
    }

    @Test public void example() { // Unlisted TLD.
        checkPublicSuffix("example", null);
        checkPublicSuffix("example.example", null);
        checkPublicSuffix("b.example.example", null);
        checkPublicSuffix("a.b.example.example", null);
    }

    @Test public void local() { // Listed, but non-Internet, TLD.
        checkPublicSuffix("local", null);
        checkPublicSuffix("example.local", null);
        checkPublicSuffix("b.example.local", null);
        checkPublicSuffix("a.b.example.local", null);
    }

    @Test public void single() { // TLD with only 1 rule.
        checkPublicSuffix("biz", null);
        checkPublicSuffix("domain.biz", "domain.biz");
        checkPublicSuffix("b.domain.biz", "domain.biz");
        checkPublicSuffix("a.b.domain.biz", "domain.biz");
    }

    @Test public void tiers() { // TLD with some 2-level rules.
        checkPublicSuffix("com", null);
        checkPublicSuffix("example.com", "example.com");
        checkPublicSuffix("b.example.com", "example.com");
        checkPublicSuffix("a.b.example.com", "example.com");
//        checkPublicSuffix("uk.com", null); // subleased record, can actually be registered
        checkPublicSuffix("example.uk.com", "example.uk.com");
        checkPublicSuffix("b.example.uk.com", "example.uk.com");
        checkPublicSuffix("a.b.example.uk.com", "example.uk.com");
        checkPublicSuffix("test.ac", "test.ac");
    }

    @Test public void wildcard() { // TLD with only 1 (wildcard) rule.
        checkPublicSuffix("cy", null);
        checkPublicSuffix("c.cy", null);
        checkPublicSuffix("b.c.cy", "b.c.cy");
        checkPublicSuffix("a.b.c.cy", "b.c.cy");
    }

    @Test public void complex() { // More complex TLD.

        checkPublicSuffix("jp", null);
        checkPublicSuffix("test.jp", "test.jp");
        checkPublicSuffix("www.test.jp", "test.jp");
        checkPublicSuffix("ac.jp", null);
        checkPublicSuffix("test.ac.jp", "test.ac.jp");
        checkPublicSuffix("www.test.ac.jp", "test.ac.jp");
        checkPublicSuffix("kyoto.jp", null);
        checkPublicSuffix("test.kyoto.jp", "test.kyoto.jp");
        checkPublicSuffix("ide.kyoto.jp", null);
        checkPublicSuffix("b.ide.kyoto.jp", "b.ide.kyoto.jp");
        checkPublicSuffix("a.b.ide.kyoto.jp", "b.ide.kyoto.jp");
        checkPublicSuffix("c.kobe.jp", null);
        checkPublicSuffix("b.c.kobe.jp", "b.c.kobe.jp");
        checkPublicSuffix("a.b.c.kobe.jp", "b.c.kobe.jp");
        checkPublicSuffix("city.kobe.jp", "city.kobe.jp");
        checkPublicSuffix("www.city.kobe.jp", "city.kobe.jp");
    }

    @Test public void exceptions() { // TLD with a wildcard rule and exceptions.
        checkPublicSuffix("om", null);
        checkPublicSuffix("test.om", null);
        checkPublicSuffix("b.test.om", "b.test.om");
        checkPublicSuffix("a.b.test.om", "b.test.om");
        checkPublicSuffix("songfest.om", "songfest.om");
        checkPublicSuffix("www.songfest.om", "songfest.om");
    }

    @Test public void usk12() { // US K12.
        checkPublicSuffix("us", null);
        checkPublicSuffix("test.us", "test.us");
        checkPublicSuffix("www.test.us", "test.us");
        checkPublicSuffix("ak.us", null);
        checkPublicSuffix("test.ak.us", "test.ak.us");
        checkPublicSuffix("www.test.ak.us", "test.ak.us");
        checkPublicSuffix("k12.ak.us", null);
        checkPublicSuffix("test.k12.ak.us", "test.k12.ak.us");
        checkPublicSuffix("www.test.k12.ak.us", "test.k12.ak.us");
    }

    static void checkPublicSuffix(String name, String entry) {
        boolean isPassed = false;
        try {
            Record record = new Domain(name).getRegisteredRecord();
            boolean isNull = entry == null && record == null;
            boolean isEqual = entry != null && record != null && entry.equals(record.getEntry());
            isPassed = isNull || isEqual;
            if (!isPassed) {
                System.out.println("Fail! " + name + " => " + record.getEntry() + " != " + entry);
            }
        } catch (MalformedDomainException e) {
            isPassed = entry == null;
            if (!isPassed) {
                System.out.println("Fail! " + e.getMessage() + " (" + e.getDomain() + ")");
            }
        }
        Assert.assertTrue(isPassed);
    }

}
