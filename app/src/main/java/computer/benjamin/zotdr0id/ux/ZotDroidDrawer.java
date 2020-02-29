package computer.benjamin.zotdr0id.ux;


import android.util.SparseArray;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.activity.MainActivity;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;

/**
 * Created by oni on 03/01/2018.
 * We have multiple ZotDroidDrawer - one for each group. At minimum we have a local group.
 */

public class ZotDroidDrawer extends com.unnamed.b.atv.view.AndroidTreeView {

    SparseArray<Collection> _collection_list_map = new SparseArray<Collection>();
    Collection _parent;
    ZotDroidNewDrawer _callback;

    TreeNode _root;

    public ZotDroidDrawer(MainActivity activity, ZotDroidNewDrawer callback) {
        super(activity);
        _callback = callback;
        _root = TreeNode.root();
        setRoot(_root);

        setDefaultAnimation(true);
        setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        setDefaultViewHolder(IconTreeItemHolder.class);
        //setDefaultNodeClickListener(nodeClickListener);
        //setDefaultNodeLongClickListener(nodeLongClickListener);
    }

    public void populate(final ZotDroidMem mem) {
        // Add the group and all sub collections
        Vector<Group> groups = mem.getGroups();
        for (final Group g : groups) {
            Vector<Collection> top_cols = mem.getGroupTopCollections(g);
            IconTreeItemHolder.IconTreeItem nodeItem = new IconTreeItemHolder.IconTreeItem(
                    R.string.ic_archive, g.get_title());
            TreeNode gnode = new TreeNode(nodeItem);
            // Top group label will show ALL the things just like Zotero proper
            TreeNode.TreeNodeClickListener listener_group = new TreeNode.TreeNodeClickListener() {
                @Override
                public void onClick(TreeNode node, Object value) {
                    _callback.onNewDrawerSelected(g, null);
                }
            };
            gnode.setClickListener(listener_group);

            gnode.setViewHolder(new IconTreeItemHolder(ZotDroidApp.getContext()));
            _root.addChild(gnode);

            Collections.sort(top_cols, new Comparator<Collection>() {
                @Override
                public int compare(Collection c0, Collection c1) {
                    return c0.get_title().compareTo(c1.get_title());
                }
            });

            // Now recurse through
            for (Collection c : top_cols) {
                _recurse_add(g, c, gnode, mem);
            }
        }
    }

    // Recursively add the members of the collection
    private void _recurse_add(final Group group, final Collection c_collection, TreeNode c_node,
                              final ZotDroidMem mem) {
        TreeNode new_node;

        if (c_collection.get_sub_collections().size() == 0) {
            new_node = new TreeNode(c_collection.get_title());
            new_node.setViewHolder(new SelectableItemHolder(ZotDroidApp.getContext()));
        } else {
            IconTreeItemHolder.IconTreeItem nodeItem = new IconTreeItemHolder.IconTreeItem(
                    R.string.ic_folder, c_collection.get_title());
            new_node = new TreeNode(nodeItem);
            new_node.setViewHolder(new IconTreeItemHolder(ZotDroidApp.getContext()));
        }

        TreeNode.TreeNodeClickListener listener_col = new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {
                _callback.onNewDrawerSelected(group, c_collection);
            }
        };
        new_node.setClickListener(listener_col);
        c_node.addChild(new_node);

        Vector<Collection> children = c_collection.get_sub_collections();
        for (Collection c : children) {
            _recurse_add(group, c, new_node, mem);
        }

    }

    /*private void setInteractions(Context context, Collection top_collection, Vector<Collection> collections, final Group group){
        ArrayList< String > collection_list = new ArrayList< String > ();



        for (Collection c : collections) {
            collection_list.add(c.get_title());
            _collection_list_map.put(_collection_list_map.size(),c);
        }

        if (top_collection != null){
            collection_list.add("<- BACK"); // TODO - eventually pass in from the R.String bit
            _collection_list_map.put(_collection_list_map.size(),top_collection);
        }

        // Override the adapter so we can set the fontsize
        this.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, collection_list ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView tv = (TextView) row;
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                String font_size = settings.getString("settings_font_size", "medium");
                // Set fonts here too!
                if (font_size.contains("small")) {
                    tv.setTextAppearance(this.getContext(), R.style.SideList_Small);
                } else if (font_size.contains("medium")) {
                    tv.setTextAppearance(this.getContext(), R.style.SideList_Medium);
                } else if (font_size.contains("large")) {
                    tv.setTextAppearance(this.getContext(), R.style.SideList_Large);
                } else {
                    tv.setTextAppearance(this.getContext(), R.style.SideList_Medium);
                }

                return row;
            }
        });

        // On-click show only these items in a particular collection and set the title to reflect this.

        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Collection filter = _collection_list_map.get(position);
                if (filter == _parent){
                    _callback.onBackDrawerSelected(group,_parent);
                } else {
                    _callback.onNewDrawerSelected(_group, filter);
                }
            }
        });
    }*/
}
