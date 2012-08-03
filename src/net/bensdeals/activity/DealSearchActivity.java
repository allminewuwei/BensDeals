package net.bensdeals.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.inject.Inject;
import net.bensdeals.R;
import net.bensdeals.adapter.SearchAdapter;
import net.bensdeals.listener.OnSearchListener;
import net.bensdeals.model.wrapper.SearchResponseWrapper;
import net.bensdeals.network.callbacks.TaskCallback;
import net.bensdeals.network.core.RemoteTask;
import net.bensdeals.network.request.SearchRequest;
import net.bensdeals.utils.IntentUtil;
import net.bensdeals.views.EmptyContentView;
import net.bensdeals.views.SearchEditView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import roboguice.util.Strings;

import java.util.List;

import static net.bensdeals.utils.IntentExtra.PREFIX_EXTRA;

public class DealSearchActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    @InjectView(R.id.deal_search_list_view) ListView listView;
    @InjectResource(R.string.shopping_api_key) String key;
    @Inject SearchEditView editView;
    @Inject EmptyContentView footer;
    @Inject SearchAdapter adapter;
    @Inject RemoteTask remoteTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deal_search_layout);
        editView.setEditText(getIntent().getStringExtra(PREFIX_EXTRA));
        editView.setOnSearchListener(new SearchListener());
        listView.addHeaderView(editView, null, false);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public static Intent intentFor(Context context, String prefix) {
        return new Intent(context, DealSearchActivity.class).putExtra(PREFIX_EXTRA, prefix);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchResponseWrapper.SearchItemWrapper item = adapter.getItem(i);
        if (item != null && !Strings.isEmpty(item.item.getLink())) {
            IntentUtil.intentForWeb(this, item.item.getLink());
        }
    }

    private class SearchListener implements OnSearchListener {
        @Override
        public void onSearch(final String searchText) {
            if (!Strings.isEmpty(searchText)) {
                createLoadingDialog(getString(R.string.searching));
                remoteTask.makeRequest(new SearchRequest(key, searchText), new TaskCallback<SearchResponseWrapper>(){
                    @Override
                    public void onTaskSuccess(SearchResponseWrapper response) {
                        super.onTaskSuccess(response);
                        List<SearchResponseWrapper.SearchItemWrapper> items = response.getItems();
                        if(!items.isEmpty()){
                            footer.hide();
                            adapter.replaceAll(items);
                        } else {
                            footer.show();
                        }
                    }

                    @Override
                    public void onTaskComplete() {
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    }
                });
            } else {
                Toast.makeText(DealSearchActivity.this, R.string.invalid_input, Toast.LENGTH_LONG).show();
            }
        }
    }
}