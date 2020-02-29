package computer.benjamin.zotdr0id.ux;

import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;

/**
 * Created by oni on 03/01/2018.
 * Called when we are changing the drawer
 */

public interface ZotDroidNewDrawer {
    void onNewDrawerSelected(Group g, Collection c);
}
