package computer.benjamin.zotdr0id.ux;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.file.FileManager;

/**
 * Created by oni on 14/07/2017.
 * http://android-coding.blogspot.co.uk/2014/02/expandablelistview-example.html
 *
 * It's likely there are better ways to do this, somewhere :/ We can't
 * associate well with the backing datastructure :/
 */

/**
 * Our main class that deals with the big list of Zotero records.
 */
public class ZotDroidListAdapter extends BaseExpandableListAdapter {

    private Activity _activity;
    private ArrayList<String> _list_group;
    private HashMap<String, ArrayList<String>> _list_child;
    private String _font_size;

    public ZotDroidListAdapter(Activity activity, ArrayList<String> groups, HashMap<String, ArrayList<String>> children, String fontsize ) {
        super();
        // TODO - eventually we wont pass in anything - we will generate from ZotDroidApp memory pool directly
        _list_group = groups;
        _list_child = children;
        _font_size = fontsize;
        _activity = activity; // Probably shouldn't have this here but I dont think it matters too much.
    }

    @Override
    public int getGroupCount() {
        return _list_group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return _list_child.get(_list_group.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return _list_group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return _list_child.get(_list_group.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater)_activity.getBaseContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.main_list_group, null);
        }

        String textGroup = (String)getGroup(groupPosition);
        TextView textViewGroup = (TextView)convertView.findViewById(R.id.main_list_group);
        textViewGroup.setText(textGroup);

        try {
            // TODO - this duplicates stuff in main. Ideally we would have this elsewhere and with static final strings
            if (_font_size.contains("small")) {
                textViewGroup.setTextAppearance(_activity.getBaseContext(), R.style.MainList_Title_Small);
            } else if (_font_size.contains("medium")) {
                textViewGroup.setTextAppearance(_activity.getBaseContext(), R.style.MainList_Title_Medium);
            } else if (_font_size.contains("large")) {
                textViewGroup.setTextAppearance(_activity.getBaseContext(), R.style.MainList_Title_Large);
            } else {
                textViewGroup.setTextAppearance(_activity.getBaseContext(), R.style.MainList_Title_Medium);
            }
        } catch (Exception e){
            // Pass for now. Rarely occurs
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater =
                    (LayoutInflater)_activity.getBaseContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.main_list_item, null);
        }

        TextView textViewItem = (TextView)convertView.findViewById(R.id.main_list_subtext);
        try {
            // TODO - this duplicates stuff in main. Ideally we would have this elsewhere and with static final strings
            if (_font_size.contains("small")) {
                textViewItem.setTextAppearance(_activity.getBaseContext(), R.style.MainList_SubText_Small);
            } else if (_font_size.contains("medium")) {
                textViewItem.setTextAppearance(_activity.getBaseContext(), R.style.MainList_SubText_Medium);
            } else if (_font_size.contains("large")) {
                textViewItem.setTextAppearance(_activity.getBaseContext(), R.style.MainList_SubText_Large);
            } else {
                textViewItem.setTextAppearance(_activity.getBaseContext(), R.style.MainList_Title_Medium);
            }
        } catch (Exception e){
            // Pass for now
        }

        String text = (String)getChild(groupPosition, childPosition);
        textViewItem.setText(text);
        ImageView imgViewChild = (ImageView) convertView.findViewById(R.id.main_list_icon_download);

        // Assume group position matches the order in the mem
        // TODO - one day we wont need to do this sort of lookup but since it's just for the filename its ok for now
        ZotDroidApp app = (ZotDroidApp) _activity.getApplication();
        Top record = app.getMem().getTops().get(groupPosition);

        if (text.contains("Attachment")) {
            String filename = text.substring(Constants.ATTACHMENT_FILENAME_POS);

            Attachment attachment = null;
            for (Attachment a : record.get_attachments()){
                if (a.get_file_name().equals(filename)){
                    attachment = a;
                    break;
                }
            }
            if (attachment != null && FileManager.attachmentExists(attachment)) {
                String uri = "@android:drawable/presence_online";
                int imageResource = _activity.getBaseContext().getResources().getIdentifier(uri, null, _activity.getBaseContext().getPackageName());
                imgViewChild.setImageResource(imageResource);
                imgViewChild.setVisibility(View.VISIBLE);
                imgViewChild.invalidate();
            } else {
                String uri = "@android:drawable/presence_invisible";
                int imageResource = _activity.getBaseContext().getResources().getIdentifier(uri, null, _activity.getBaseContext().getPackageName());
                imgViewChild.setImageResource(imageResource);
                imgViewChild.setVisibility(View.VISIBLE);
                imgViewChild.invalidate();
            }
        } else {
            // Hide icon for normal meta-data fields
            imgViewChild.setVisibility(View.INVISIBLE);
        }
        imgViewChild.invalidate();
        imgViewChild.refreshDrawableState();

        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
