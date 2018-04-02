package cn.zhao.keyboarddemo;

import android.inputmethodservice.KeyboardView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.Utils;

/**
 * @author lexu
 * @date 2018/4/2
 */
public class MainActivity extends AppCompatActivity {
    private KeyboardView mKeyboardView;
    private KeyBoardEditText mEdMain;
    private LinearLayout mLayoutMain;
    private LinearLayout mLayoutRoot;
    /**
     * 滚动距离
     */
    private int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.init(this);
        mKeyboardView = findViewById(R.id.view_keyboard);
        mLayoutMain = findViewById(R.id.layout_main);
        mLayoutRoot = findViewById(R.id.layout_root);
        mEdMain = findViewById(R.id.ed_main);

        mEdMain.setKeyboardType(mLayoutMain, mKeyboardView, true);
        mEdMain.setOnKeyListener(mListener);
    }

    private KeyBoardEditText.OnKeyStatusChangeListener mListener = new KeyBoardEditText.OnKeyStatusChangeListener() {
        @Override
        public void hide() {
            mLayoutRoot.scrollBy(0, -height);
        }

        @Override
        public void show() {
            mEdMain.post(new Runnable() {
                @Override
                public void run() {
                    int[] rect = new int[2];
                    mEdMain.getLocationOnScreen(rect);
                    int y = rect[1];

                    mKeyboardView.getLocationOnScreen(rect);
                    int keyboardY = rect[1];

                    height = y - (keyboardY - mEdMain.getMeasuredHeight());
                    mLayoutRoot.scrollBy(0, height);
                }
            });
        }
    };

    /**
     * 点击空白处隐藏键盘
     *
     * @param view
     */
    public void hideInput(View view) {
        if (mKeyboardView.getVisibility() != View.GONE) {
            mKeyboardView.setVisibility(View.GONE);
            mLayoutMain.setVisibility(View.GONE);
            mListener.hide();
        }
    }
}
