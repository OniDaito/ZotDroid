package computer.benjamin.zotdr0id;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Author;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.Summary;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("computer.benjamin.zotdr0id", appContext.getPackageName());
    }

    @Test
    public void testRecordDB() throws Exception {
        // Context of the app under test.
        // Apparently this test runs on a device - instrumentation tests essentially
        Context appContext = InstrumentationRegistry.getTargetContext();
        ZotDroidDB db = new ZotDroidDB(appContext);

        Item item = new Item("4321");
        item.set_version("1234");
        Author author = new Author("author", "4321");
        item.add_author(author);
        item.set_content_type("pdf");
        item.set_date_added("testdate");
        item.set_parent("abcd");
        item.set_title("title");
        item.set_item_type("type");
        db.writeRecord(item);
        int numrow = db.getNumRecords();

        Item r2 = db.getRecord(numrow - 1);

        assertEquals(r2.get_version(), "1234");
        assertEquals(r2.get_authors().elementAt(0).get_name(), "author");
        assertEquals(r2.get_content_type(), "pdf");
        assertEquals(r2.get_parent(), "abcd");
        assertEquals(r2.get_title(), "title");
        assertEquals(r2.get_item_type(), "type");

        db.reset();
    }

    @Test
    public void testSummaryDB() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ZotDroidDB db = new ZotDroidDB(appContext);
        Summary s = new Summary();
        db.writeSummary(s);
        Summary summary = db.getSummary();
        assertEquals(summary.get_last_version(), "0000");
        s.set_last_version("1234");
        db.writeSummary(s);
        summary = db.getSummary();
        assertEquals(summary.get_last_version(), "1234");
    }

    @Test
    public void testAttachmentDB() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ZotDroidDB db = new ZotDroidDB(appContext);

        Item item = new Item("4321");
        item.set_version("1234");
        Author author = new Author("author", "4321");
        item.add_author(author);
        item.set_content_type("pdf");
        item.set_date_added("testdate");
        item.set_parent("abcd");
        item.set_title("title");
        item.set_item_type("type");

        Attachment attachment = new Attachment();
        attachment.set_file_name("test.pdf");
        attachment.set_file_type("attachment/txt");
        attachment.set_zotero_key("abcd");
        item.addAttachment(attachment);
        db.writeRecord(item);
        db.writeAttachment(attachment);
        attachment.set_file_type("attachment/pdf");
        db.updateAttachment(attachment);

        Attachment attachment2 = db.getAttachment("abcd");
        assertEquals(attachment2.get_file_type(), "attachment/pdf");
        db.reset();
    }

    @Test
    public void testGroupDB() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ZotDroidDB db = new ZotDroidDB(appContext);
        Group group = db.getGroup(Constants.LOCAL_GROUP);
        assertEquals(group.get_title(), "Local Library");
        db.reset();
    }
}
