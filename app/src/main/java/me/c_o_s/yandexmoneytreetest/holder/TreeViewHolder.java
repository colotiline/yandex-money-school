package me.c_o_s.yandexmoneytreetest.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import me.c_o_s.yandexmoneytreetest.R;
import me.c_o_s.yandexmoneytreetest.model.Category;

/**
 * Created by Constantine on 8/6/2015.
 */
public class TreeViewHolder extends TreeNode.BaseNodeViewHolder<Category> {
    private TextView stateView;
    private boolean isLeaf;

    public TreeViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode treeNode, Category category) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.tree_layout, null, false);
        TextView tvValue = (TextView) view.findViewById(R.id.category);
        tvValue.setText(category.title);

        isLeaf = treeNode.isLeaf();
        stateView = (TextView) view.findViewById(R.id.state);

        if(!isLeaf) {
            stateView.setText("+");
        }

        LinearLayout container = (LinearLayout) view.findViewById(R.id.container);

        int paddingLeft = (treeNode.getLevel()-1) * 20;
        container.setPadding(convertPxToDp(paddingLeft), 0, 0, 0);

        return view;
    }

    @Override
    public void toggle(boolean active) {
        if(isLeaf) {
            return;
        }

        stateView.setText(active ? "-" : "+");
    }

    public int convertPxToDp(int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px * scale + 0.5f);
    }
}
