package computer.benjamin.zotdr0id;

import org.junit.Test;
import computer.benjamin.zotdr0id.data.ZotDroidDB;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DatabaseTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void load_db() throws Exception {

        ZotDroidDB = new ZotDroidDB();
    }


}