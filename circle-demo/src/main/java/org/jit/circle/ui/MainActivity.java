package org.jit.circle.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.guzhaolei.circledemo.adapter.CircleAdapter;
import com.guzhaolei.circledemo.bean.CircleItem;
import com.guzhaolei.circledemo.contral.CirclePublicCommentController;
import com.guzhaolei.circledemo.listener.SwipeListViewOnScrollListener;
import com.guzhaolei.circledemo.utils.CommonUtils;
import com.guzhaolei.circledemo.utils.DatasUtil;
import com.guzhaolei.circledemo.utils.HeightComputable;
import com.guzhaolei.circledemo.widgets.MultiImageView;

import org.jit.circle.R;

import java.util.List;

/**
 * @author guzhaolei
 * @ClassName: MainActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018-7-1 下午4:21:18
 */
public class MainActivity extends Activity implements OnRefreshListener, HeightComputable {

    protected static final String TAG = MainActivity.class.getSimpleName();
    private ListView mCircleLv;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CircleAdapter mAdapter;
    private LinearLayout mEditTextBody;
    private EditText mEditText;
    private TextView sendTv;

    private int mScreenHeight;
    private int mEditTextBodyHeight;
    private CirclePublicCommentController mCirclePublicCommentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadData();
    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mRefreshLayout);
        mCircleLv = (ListView) findViewById(R.id.circleLv);
        mCircleLv.setOnScrollListener(new SwipeListViewOnScrollListener(mSwipeRefreshLayout));
        mCircleLv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEditTextBody.getVisibility() == View.VISIBLE) {
                    mEditTextBody.setVisibility(View.GONE);
                    CommonUtils.hideSoftInput(MainActivity.this, mEditText);
                    return true;
                }
                return false;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mAdapter = new CircleAdapter(this);
        mAdapter.setOnMultiImageItemClickListener(new MultiImageView.OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, int position) {
                MultiImageView multiImageView = (MultiImageView) parent;
                ImagePagerActivity.imageSize = new ImageSize(view.getWidth(), view.getHeight());
                ImagePagerActivity.startImagePagerActivity(MainActivity.this, multiImageView.getImagesList(), position);
            }
        });
        mCircleLv.setAdapter(mAdapter);
        mEditTextBody = (LinearLayout) findViewById(R.id.editTextBodyLl);
        mEditText = (EditText) findViewById(R.id.circleEt);
        sendTv = (TextView) findViewById(R.id.sendTv);

        mCirclePublicCommentController = new CirclePublicCommentController(this, mEditTextBody, mEditText, sendTv);
        mCirclePublicCommentController.setListView(mCircleLv);
        mAdapter.setCirclePublicCommentController(mCirclePublicCommentController);

        setViewTreeObserver();
    }


    private void setViewTreeObserver() {

        final ViewTreeObserver swipeRefreshLayoutVTO = mSwipeRefreshLayout.getViewTreeObserver();
        swipeRefreshLayoutVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mSwipeRefreshLayout.getWindowVisibleDisplayFrame(r);
                int screenH = mSwipeRefreshLayout.getRootView().getHeight();
                int keyH = screenH - (r.bottom - r.top);
                if (keyH == CommonUtils.keyboardHeight) {//有变化时才处理，否则会陷入死循环
                    return;
                }
                Log.d(TAG, "keyH = " + keyH + " &r.bottom=" + r.bottom + " &top=" + r.top);
                CommonUtils.keyboardHeight = keyH;
                mScreenHeight = screenH;//应用屏幕的高度
                mEditTextBodyHeight = mEditTextBody.getHeight();
                if (mCirclePublicCommentController != null) {
                    mCirclePublicCommentController.handleListViewScroll();
                }
            }
        });
    }

    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);

    }

    private void loadData() {
        List<CircleItem> datas = DatasUtil.createCircleDatas();
        mAdapter.setDatas(datas);
        mAdapter.notifyDataSetChanged();
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getEditTextBodyHeight() {
        return mEditTextBodyHeight;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mEditTextBody != null && mEditTextBody.getVisibility() == View.VISIBLE) {
                mEditTextBody.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
