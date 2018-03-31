package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.HandlerThread;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import java.util.logging.Handler;

import ru.noties.markwon.spans.HeadingSpan;

/**
 * Created by elanimus on 3/10/18.
 */

public class BetterPopupWindow  {
    protected final View anchor;
    private final PopupWindow window;
    public View root;
    private Drawable background = null;
    public final WindowManager windowManager;

    public BetterPopupWindow(View anchor, int layout) {
        this.layout = layout;
        this.anchor = anchor;
        this.window = new PopupWindow(anchor.getContext());
        this.window.setWidth(AndroidUtilities.dp(230));
        this.window.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    BetterPopupWindow.this.window.dismiss();
                    return true;
                }
                return false;
            }
        });

        this.windowManager = (WindowManager) this.anchor.getContext().getSystemService(Context.WINDOW_SERVICE);
        onCreate();
    }

    private int layout;

    protected void onCreate() {
        LayoutInflater inflater =
                (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup root = (ViewGroup) inflater.inflate(layout, null);

        this.setContentView(root);
    }


    protected void onShow() {}

    private void preShow() {
        if(this.root == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }
        onShow();

        if(this.background == null) {
            this.window.setBackgroundDrawable(new BitmapDrawable());
        } else {
            this.window.setBackgroundDrawable(this.background);
        }

        this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.window.setTouchable(true);
        this.window.setFocusable(true);
        this.window.setOutsideTouchable(true);
        Object[] x = editHand.selectionSpans();
        for(int m = 0; m < x.length; m++){
            //if(x[m] instanceof ru.noties.markwon.spans.ThematicBreakSpan){
            //} else
            if(x[m] instanceof ru.noties.markwon.spans.LinkSpan){
                link.setActivated(true);
            } else if(x[m] instanceof ru.noties.markwon.spans.EmphasisSpan){
                ital.setActivated(true);
            }else if(x[m] instanceof ru.noties.markwon.spans.BlockQuoteSpan){
                //quote.setActivated(true);
            }else if(x[m] instanceof ru.noties.markwon.spans.StrongEmphasisSpan){

            }else if(x[m] instanceof UnderlineSpan){
                //under.setActivated(true);
            }else if(x[m] instanceof StrikethroughSpan){
                strike.setActivated(true);
            } else if(x[m] instanceof CharacterStyle){
                //new CharacterStyle(Bold)
            }else if(x[m] instanceof LeadingMarginSpan) {

            }
            //set activateds
            //Log.d("SPAN",x[m].toString());
        }

        this.window.setContentView(this.root);
    }


    public void setBackgroundDrawable(Drawable background) {
        this.background = background;
    }

    public interface  OnEditDelegate {
        void onButtonPressed(int which);
        Object [] selectionSpans();
        void onClear(int which);
    }

    OnEditDelegate editHand;

    void setEditHandler(OnEditDelegate hand){
        editHand = hand;
    }

    public void setContentView(View root) {
        //if(layout==R.layout.options) {
            this.root =  root;
        //}else this.root =
        this.window.setContentView(root);
        if(layout==R.layout.options) {
         bold = root.findViewById(R.id.bold);
            bold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.bold);
                    dismiss();
                }
            });
            horiz = root.findViewById(R.id.horiz);
            horiz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.horiz);
                    dismiss();
                }
            });
         ital = root.findViewById(R.id.italic);
            ital.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.italic);
                    dismiss();
                }
            });

         quote = root.findViewById(R.id.quote);
            quote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.quote);

                    dismiss();
                }
            });
         size1 = root.findViewById(R.id.size1);
            size1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.size1);
                    dismiss();
                }
            });
            final ImageButton clear = root.findViewById(R.id.clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onClear(R.id.clear);
                    editHand.onButtonPressed(R.id.clear);
                    dismiss();
                }
            });
         size2 = root.findViewById(R.id.size2);
            size2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.size2);
                    dismiss();
                }
            });

         link = root.findViewById(R.id.link);
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.link);
                    dismiss();
                }
            });
         video = root.findViewById(R.id.video);
            video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.video);
                    dismiss();
                }
            });
         image = root.findViewById(R.id.image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.image);
                    dismiss();
                }
            });
         strike = root.findViewById(R.id.strike);
            strike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.strike);
                    dismiss();
                }
            });
         under = root.findViewById(R.id.underline);
            under.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.underline);
                    dismiss();
                }
            });
        }else {
         remove = root.findViewById(R.id.remove);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHand.onButtonPressed(R.id.remove);
                    dismiss();
                }
            });
        }
    }

    ImageButton horiz ;

    ImageButton bold ;
    ImageButton ital ;
    ImageButton quote ;
    ImageButton size1;
    ImageButton clear ;
    ImageButton size2 ;
    ImageButton squote ;
    ImageButton link ;
    ImageButton video ;
    ImageButton image ;
    ImageButton strike ;
    ImageButton under ;
    ImageButton remove ;

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        this.window.setOnDismissListener(listener);
    }


    public void showLikePopDownMenu() {
        this.showLikePopDownMenu(0, 0);
    }

    public void showLikePopDownMenu(int xOffset, int yOffset) {
        this.preShow();

        this.window.setAnimationStyle(R.style.AnimationsX_PopDownMenu);

        this.window.showAsDropDown(this.anchor, xOffset, yOffset);
    }


    public void showLikeQuickAction() {
        this.showLikeQuickAction(0, 0);
    }


    public void showLikeQuickAction(int xOffset, int yOffset) {
        this.preShow();

        this.window.setAnimationStyle(R.style.AnimationsX_GrowFromBottom);

        int[] location = new int[2];
        this.anchor.getLocationOnScreen(location);

        Rect anchorRect =
                new Rect(location[0], location[1], location[0] + this.root.getWidth(), location[1]
                        + this.root.getHeight());

        this.root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootWidth = this.root.getMeasuredWidth();
        int rootHeight = this.root.getMeasuredHeight();

        int screenWidth = this.windowManager.getDefaultDisplay().getWidth();

        int xPos = ((screenWidth - rootWidth) / 2) + xOffset;
        int yPos = anchorRect.top - rootHeight + yOffset;

        // display on bottom
        if(rootHeight > anchorRect.top) {
            yPos = anchorRect.bottom + yOffset;
            this.window.setAnimationStyle(R.style.AnimationsX_GrowFromTop);
        }

        this.window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    public void dismiss() {

        this.window.dismiss();
    }
}
