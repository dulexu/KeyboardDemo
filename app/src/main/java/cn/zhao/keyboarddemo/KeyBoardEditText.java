package cn.zhao.keyboarddemo;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.KeyboardUtils;

import java.util.List;

/**
 * @author lexu
 * @date 2018/4/2
 */

public class KeyBoardEditText extends AppCompatEditText {
    private Context mContext;
    private Keyboard mKeyboardNumber;
    private Keyboard mkeyboardLetter;
    private KeyboardView mKeyboardView;
    private ViewGroup mViewGroup;
    /**
     * 是否发生键盘切换
     */
    private boolean mChangeLetter = false;
    /**
     * 是否为大写字母
     */
    private boolean mIsCapital = false;

    public OnKeyStatusChangeListener mListener;

    public void setOnKeyListener(OnKeyStatusChangeListener listener) {
        mListener = listener;
    }


    public interface OnKeyStatusChangeListener {
        /**
         * 隐藏键盘
         */
        void hide();

        /**
         * 显示键盘
         */
        void show();
    }

    public KeyBoardEditText(Context context) {
        super(context);
        mContext = context;
        initEditView();
    }

    public KeyBoardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initEditView();
    }

    public KeyBoardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initEditView();
    }

    private void initEditView() {
        mkeyboardLetter = new Keyboard(mContext, R.xml.keyboard_letter);
        mKeyboardNumber = new Keyboard(mContext, R.xml.keyboard_number);
    }

    public void setKeyboardType(ViewGroup viewGroup, KeyboardView keyboardView, boolean number) {
        mViewGroup = viewGroup;
        mKeyboardView = keyboardView;
        if (number) {
            mKeyboardView.setKeyboard(mKeyboardNumber);
        }
        mKeyboardView.setPreviewEnabled(true);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            // 当用户按下一个键时调用。在调用onKey之前调用。如果之前定义的codes有问题，primaryCode为0
            @Override
            public void onPress(int primaryCode) {
                canShowPreview(primaryCode);
            }

            // 当用户释放键时调用
            @Override
            public void onRelease(int i) {

            }

            // 之前codes字段定义的值
            @Override
            public void onKey(int primaryCode, int[] ints) {
                Editable editable = getText();
                int start = getSelectionStart();
                // 删除功能
                if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (!TextUtils.isEmpty(editable) && start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
                // 字母键盘与数字键盘切换
                else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                    changeKeyBoard(!mChangeLetter);
                }
                // 完成
                else if (primaryCode == Keyboard.KEYCODE_DONE) {
                    mKeyboardView.setVisibility(GONE);
                    mViewGroup.setVisibility(GONE);
                    mListener.hide();
                }
                // 切换大小写
                else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                    changeCapital(!mIsCapital);
                    mKeyboardView.setKeyboard(mkeyboardLetter);
                } else {
                    editable.insert(start, String.valueOf(Character.toChars(primaryCode)));
                }
            }

            // 如果之前在keyOutputText定义过数值，则按键之后会在此回调中进行响应
            @Override
            public void onText(CharSequence charSequence) {

            }

            // 下面都是在键盘上进行手势操作
            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });
    }

    private void canShowPreview(int primaryCode) {
        int[] nolists = {Keyboard.KEYCODE_SHIFT, Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_CANCEL,
                Keyboard.KEYCODE_DONE, Keyboard.KEYCODE_DELETE, Keyboard.KEYCODE_ALT, 32};
        boolean isEnabled = false;
        for (int code : nolists) {
            if (code == primaryCode) {
                isEnabled = true;
            }
        }
        mKeyboardView.setPreviewEnabled(isEnabled);
    }

    /**
     * 大小写相互切换
     *
     * @param isCapital
     */
    private void changeCapital(boolean isCapital) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        List<Keyboard.Key> keyList = mkeyboardLetter.getKeys();
        for (Keyboard.Key key : keyList) {
            if (key.label != null && lowercase.contains(key.label.toString().toLowerCase())) {
                if (isCapital) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] -= 32;
                } else {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] += 32;
                }
            }
            if (key.label != null && "小写".equals(key.label) && isCapital) {
                key.label = "大写";
            } else if (key.label != null && "大写".equals(key.label) && !isCapital) {
                key.label = "小写";
            }
            if (key.label != null && key.label == "space") {
                key.label = "空格";
            }
        }
        mIsCapital = isCapital;
    }

    /**
     * 数字键盘与字母键盘相互切换
     *
     * @param letter
     */
    private void changeKeyBoard(boolean letter) {
        mChangeLetter = letter;
        if (mChangeLetter) {
            mKeyboardView.setKeyboard(mkeyboardLetter);
        } else {
            mKeyboardView.setKeyboard(mKeyboardNumber);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        KeyboardUtils.hideSoftInput(this);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mKeyboardView.getVisibility() != View.VISIBLE) {
                mKeyboardView.setVisibility(View.VISIBLE);
                mViewGroup.setVisibility(View.VISIBLE);
                mListener.show();
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mKeyboardView.getVisibility() != View.GONE
                && mViewGroup.getVisibility() != View.GONE) {
            mKeyboardView.setVisibility(View.GONE);
            mViewGroup.setVisibility(View.GONE);
            mListener.hide();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyboardUtils.hideSoftInput(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyboardUtils.hideSoftInput(this);
    }

}
