package me.c_o_s.yandexmoneytreetest.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.c_o_s.yandexmoneytreetest.R;
import me.c_o_s.yandexmoneytreetest.database.YandexMoneyContract;
import me.c_o_s.yandexmoneytreetest.database.YandexMoneyDbHelper;
import me.c_o_s.yandexmoneytreetest.holder.TreeViewHolder;
import me.c_o_s.yandexmoneytreetest.model.Category;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout _layout;
    private AndroidTreeView _treeView;
    private TreeNode _categoriesTreeRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _layout = (SwipeRefreshLayout) findViewById(R.id.layout);
        _layout.setOnRefreshListener(this);

        initializeTree();
    }

    private void initializeTree() {
        boolean wasUpdated = updateCategoriesTreeViewFromDb();

        if(!wasUpdated) {
            updateCategoriesTreeViewFromServer();
        }
    }

    private void updateCategoriesTreeView(List<Category> categories) {
        if(_categoriesTreeRoot == null) {
            _categoriesTreeRoot = TreeNode.root();
        }

        removeCategoriesFromCategoriesTreeView();
        addCategoriesToTree(categories, _categoriesTreeRoot);
        addCategoriesToTreeView();
    }

    private void addCategoriesToTreeView() {
        if(_treeView == null) {
            _treeView = new AndroidTreeView(MainActivity.this, _categoriesTreeRoot);
            _treeView.setDefaultViewHolder(TreeViewHolder.class);
            _layout.addView(_treeView.getView());

            return;
        }

        List<TreeNode> nodesToAdd = new ArrayList<>();

        for (TreeNode child : _categoriesTreeRoot.getChildren()) {
            nodesToAdd.add(child);
        }

        for (TreeNode node : nodesToAdd) {
            _treeView.addNode(_categoriesTreeRoot, node);
        }
    }

    private void removeCategoriesFromCategoriesTreeView() {
        if(_treeView != null) {
            List<TreeNode> nodesToRemove = new ArrayList<>();

            for (TreeNode child : _categoriesTreeRoot.getChildren()) {
                nodesToRemove.add(child);
            }

            for (TreeNode removeChild : nodesToRemove) {
                _treeView.removeNode(removeChild);
                _categoriesTreeRoot.deleteChild(removeChild);
            }
        }
    }

    private void addCategoriesToTree(List<Category> categories, TreeNode root) {
        for (Category category:categories) {
            TreeNode categoryTreeNode = new TreeNode(category);

            root.addChild(categoryTreeNode);

            if(category.subs != null) {
                addCategoriesToTree(category.subs, categoryTreeNode);
            }
        }
    }

    private void setCategoriesInDb(List<Category> categories) {
        YandexMoneyDbHelper dbHelper = new YandexMoneyDbHelper(MainActivity.this);
        SQLiteDatabase writableDb = dbHelper.getWritableDatabase();

        writableDb.delete(YandexMoneyContract.CategoryEntry.TABLE_NAME, "1=1", null);

        for (Category category : categories) {
            insertCategoryInDb(writableDb, category, null);
        }
    }

    private void insertCategoryInDb(SQLiteDatabase db, Category category, Category parentCategory) {
        ContentValues values = new ContentValues();
        values.put(YandexMoneyContract.CategoryEntry.COLUMN_NAME_TITLE, category.title);
        values.put(YandexMoneyContract.CategoryEntry.COLUMN_NAME_YANDEX_ID, category.id);
        values.put(YandexMoneyContract.CategoryEntry.COLUMN_NAME_PARENT_ID,
                parentCategory != null ? parentCategory._id : 0);

        long _id = db.insert(YandexMoneyContract.CategoryEntry.TABLE_NAME, null, values);
        category._id = _id;

        if(category.subs == null) {
            return;
        }

        for (Category sub:category.subs) {
            insertCategoryInDb(db, sub, category);
        }
    }

    private void updateCategoriesTreeViewFromServer() {
        String categoriesUrl = "https://money.yandex.ru/api/categories-list";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest categoriesRequest = new StringRequest(Request.Method.GET, categoriesUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                List<Category> categories = Arrays.asList(gson.fromJson(response, Category[].class));

                updateCategoriesTreeView(categories);
                _layout.setRefreshing(false);
                displayCategoriesUpdatedFromServerToast();

                setCategoriesInDb(categories);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                _layout.setRefreshing(false);
                displayCantDownloadToast();
            }
        });
        requestQueue.add(categoriesRequest);
    }

    private void displayCategoriesUpdatedFromServerToast() {
        Toast.makeText(getApplicationContext(), "Дерево обновлено с сервера", Toast.LENGTH_SHORT).show();
    }

    private void displayCantDownloadToast() {
        Toast.makeText(getApplicationContext(), "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
    }

    private boolean updateCategoriesTreeViewFromDb() {
        YandexMoneyDbHelper dbHelper = new YandexMoneyDbHelper(MainActivity.this);
        SQLiteDatabase readableDb = dbHelper.getReadableDatabase();

        String[] categoriesQueryProjection = new String[] {
                YandexMoneyContract.CategoryEntry._ID,
                YandexMoneyContract.CategoryEntry.COLUMN_NAME_TITLE,
                YandexMoneyContract.CategoryEntry.COLUMN_NAME_YANDEX_ID,
                YandexMoneyContract.CategoryEntry.COLUMN_NAME_PARENT_ID
        };

        Cursor categoryEntriesCursor = readableDb.query(
                YandexMoneyContract.CategoryEntry.TABLE_NAME,
                categoriesQueryProjection,
                YandexMoneyContract.CategoryEntry.COLUMN_NAME_PARENT_ID + "=0",
                null,
                null,
                null,
                YandexMoneyContract.CategoryEntry._ID + " ASC"
        );

        if(categoryEntriesCursor.getCount() == 0) {
            return false;
        }

        List<Category> categories = getCategoriesFromCursor(readableDb, categoriesQueryProjection,
                categoryEntriesCursor);

        updateCategoriesTreeView(categories);
        displayCategoriesUpdatedFromDbToast();

        return true;
    }

    private List<Category> getCategoriesFromCursor(SQLiteDatabase readableDb, String[] projection, Cursor cursor) {
        cursor.moveToFirst();

        List<Category> categories = new ArrayList<>();

        do {
            Category category = new Category();

            category._id = cursor.getInt(cursor.getColumnIndexOrThrow(YandexMoneyContract.CategoryEntry._ID));
            category.id = cursor.getInt(cursor.getColumnIndexOrThrow(YandexMoneyContract.CategoryEntry.COLUMN_NAME_YANDEX_ID));
            category.title = cursor.getString(cursor.getColumnIndexOrThrow(YandexMoneyContract.CategoryEntry.COLUMN_NAME_TITLE));

            Cursor subEntriesCursor = readableDb.query(
                    YandexMoneyContract.CategoryEntry.TABLE_NAME,
                    projection,
                    YandexMoneyContract.CategoryEntry.COLUMN_NAME_PARENT_ID + "=" +
                    category._id,
                    null,
                    null,
                    null,
                    YandexMoneyContract.CategoryEntry._ID + " ASC"
            );

            if(subEntriesCursor.getCount() != 0) {
                category.subs = getCategoriesFromCursor(readableDb, projection, subEntriesCursor);
            }

            categories.add(category);
        } while (cursor.moveToNext());

        return categories;
    }

    private void displayCategoriesUpdatedFromDbToast() {
        Toast.makeText(getApplicationContext(), "Дерево обновлено из БД", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        _layout.post(new Runnable() {
            @Override
            public void run() {
                updateCategoriesTreeViewFromServer();
            }
        });
    }
}
