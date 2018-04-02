package org.telegram.ui.Components;


import android.Manifest;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.KeyListener;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.mp4parser.authoring.Edit;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.Node;
import org.telegram.tgnet.NodeElement;
import org.telegram.tgnet.NodeText;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.PhotoAlbumPickerActivity;
import org.telegram.ui.TelegraphEditor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.renderer.SpannableRenderer;
import ru.noties.markwon.spans.BlockQuoteSpan;
import ru.noties.markwon.spans.LinkSpan;
import ru.noties.markwon.spans.SpannableTheme;
import ru.noties.markwon.spans.ThematicBreakSpan;
//import ru.noties.markwon.view.IMarkwonView;
//import ru.noties.markwon.view.MarkwonViewHelper;
/**
 * Created by elanimus on 3/10/18.
 */


//extends LinearLayout !^ becomes a static inner class replace ed in Editor
//@tgp and animations maybe
//telenews, telegazeta

public class TelegraphEditText extends LinearLayout implements View.OnKeyListener,View.OnClickListener {

    public TelegraphEditText(Context context) {
        super(context);
        setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        setOrientation(LinearLayout.VERTICAL);
        TgpEdit t = new TgpEdit(context);
        addView(t);
    }

    public void setText(String body){
        TgpEdit top = (TgpEdit) getChildAt(0);
        top.setText(body);
    }

    public void setText(Editable edi){
        TgpEdit top = (TgpEdit) getChildAt(0);
        top.setText(edi);
    }

    public static int edlen;
    public static int current;
    @Override
    public void onClick(final View v) {
        int index = TelegraphEditText.this.indexOfChild(v);
        if(v instanceof PhotoView){
            removeViewAt(index);
            onReachedEnd(index);
            return;
        }
        current  = index;
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        return false;
    }
    private static class TImbed {
        int type;
        int id;
        Object pl;

        static TImbed newI() {
            return new TImbed();
        }
    }

    public interface OnLongClick {
        void longClicked(boolean show);

        void keyboardAttached(int height);

        void onRequirementChange(boolean to);

        boolean isActionModeShown();

        int titlen();

        void onRequestImage(PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate s);

    }

    public float getTSize() {
        int size = getChildCount();
        int c = 0;
        for(int x = 0; x < size; x++){
            View v = getChildAt(x);
            if(v instanceof TgpEdit){
                c += ((TgpEdit)v).length();
            }
        }
        return c;
    }

    public Editable getText() {
        int size = getChildCount();
        Editable c = Editable.Factory.getInstance().newEditable("");
        for(int x = 0; x < size; x++){
            View v = getChildAt(x);
            if(v instanceof TgpEdit){
                c.append( ((TgpEdit)v).getText());
            }
        }
        return c;
    }

    private void insertAt(final int type,final int start,Object p,final int index){
        if(type==1) {
            TgpEdit split = (TgpEdit) getChildAt(index);
            CharSequence s = split.getText();
            split.setText(s.subSequence(0,start));

            final PhotoView v = new PhotoView(TelegraphEditText.this.getContext(), 1);
            v.w.loadVideo((String) p,null,(String) p, false);
                    //loadData("<iframe src=\"" +  + "\" height=\"250\"></iframe>", "text/html", "UTF-8");
            v.link = (String) p;
            v.close.setId(index + 1 );

            addView(v, index + 1);
            //media.push(t);
            TgpEdit plus = new TgpEdit(getContext());
            plus.setText(s.subSequence(start,s.length()));
            plus.setId(index + 2);
            addView(plus,index + 2);
        }else if(type == 0){
            TgpEdit split = (TgpEdit) getChildAt(index);
            final CharSequence s = split.getText();
            split.setText(s.subSequence(0,start));
            delegate.onRequestImage(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
                @Override
                public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<Integer> ttls, ArrayList<MediaController.PhotoEntry> videos, ArrayList<ArrayList<TLRPC.InputDocument>> masks, ArrayList<MediaController.SearchImage> webPhotos) {
                    for (int a = 0; a < photos.size(); a++) {
                        String video = photos.get(a);
                        Log.d("fff",video+"");
                        final PhotoView v = new PhotoView(TelegraphEditText.this.getContext(), 0);
                        android.graphics.Point screenSize = AndroidUtilities.getRealScreenSize();
                        v.v.setImageBitmap(ImageLoader.loadBitmap(video, null, screenSize.x, screenSize.y, true));
                        v.link=video;
                        v.close.setId(index + 1);

                        addView(v, index + 1);
                        //media.push(t);
                        TgpEdit plus = new TgpEdit(getContext());
                        plus.setText(s.subSequence(start,s.length()));
                        plus.setId(index + 2);
                        addView(plus,index + 2);
                    }
                }

                @Override
                public void startPhotoSelectActivity() {
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
                        ((BaseFragment) delegate).getParentActivity().startActivityForResult(chooserIntent, 100);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });

        }
    }


    private void onReachedEnd(int inde){
        TgpEdit tbexterminated = (TgpEdit) getChildAt(inde);
        TgpEdit pre = (TgpEdit) getChildAt(inde-1);
        pre.append("\n"+tbexterminated.getText());
        removeView(tbexterminated);
        current--;

        /*int size = getChildCount();
        int c = 0;
        boolean prev = false;
        for(int x = 0; x < size; x++){
            View v = getChildAt(x);
            if(v instanceof TgpEdit){
                if(prev && tbexterminated.equals(v)){
                    TgpEdit pre = (TgpEdit) getChildAt(x-1);
                    pre.append("\n"+tbexterminated.getText());
                    removeView(tbexterminated);
                    break;
                }
               prev = true;
            }else prev = false;
        }
        */
    }

    OnLongClick delegate;
    class PhotoView extends RelativeLayout{
        ImageView v;
        WebPlayerView w;
        ImageButton close;
        String link;

        public PhotoView(Context context, int type) {
            super(context);
            setPadding(AndroidUtilities.dp(5),0,0,0);
            setElevation(AndroidUtilities.dp(10));
            setBackgroundColor(Color.parseColor("#222222"));
            setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,250));

             if(type==0) {
                 v = new ImageView(context);
                 v.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 250));
                 addView(v);
             }else{
                 w = new WebPlayerView(context, true, true, new WebPlayerView.WebPlayerViewDelegate() {
                     @Override
                     public void onInitFailed() {

                     }

                     @Override
                     public TextureView onSwitchToFullscreen(View controlsView, boolean fullscreen, float aspectRatio, int rotation, boolean byButton) {
                         return null;
                     }

                     @Override
                     public TextureView onSwitchInlineMode(View controlsView, boolean inline, float aspectRatio, int rotation, boolean animated) {
                         return null;
                     }

                     @Override
                     public void onInlineSurfaceTextureReady() {

                     }

                     @Override
                     public void prepareToSwitchInlineMode(boolean inline, Runnable switchInlineModeRunnable, float aspectRatio, boolean animated) {

                     }

                     @Override
                     public void onSharePressed() {

                     }

                     @Override
                     public void onPlayStateChanged(WebPlayerView playerView, boolean playing) {

                     }

                     @Override
                     public void onVideoSizeChanged(float aspectRatio, int rotation) {

                     }

                     @Override
                     public ViewGroup getTextureViewContainer() {
                         return null;
                     }

                     @Override
                     public boolean checkInlinePermissons() {
                         return false;
                     }
                 });
                 w.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 250));
                 addView(w);
             }


             close = new ImageButton(context);
              close.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View x) {
                    TelegraphEditText.this.onClick(PhotoView.this);
                }
            });
             close.setBackground( ApplicationLoader.applicationContext.getResources().getDrawable(type==0 ? R.drawable.ic_close_white :R.drawable.ic_close_black_24dp ));
             close.setLayoutParams(LayoutHelper.createRelative(28,28));
             addView(close);

        }

    }
    public void setLongDelegate(OnLongClick o) {
        delegate = o;
    }
    public void add(int type, Object p, Context c) {
        TImbed t = TImbed.newI();
        t.type = type;
        t.pl = p;
        //plus.setOrientation(LinearLayout.VERTICAL);
        //plus.addView(1,);
        if (type == 0) {
            PhotoView i = new PhotoView(c,0);
            //i.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper._CONTENT, Gravity.TOP));
            //i.v.setImageURI(Uri.parse("file://" + p));
            android.graphics.Point screenSize = AndroidUtilities.getRealScreenSize();
            i.v.setImageBitmap(ImageLoader.loadBitmap((String)p, null, screenSize.x, screenSize.y, true));
            i.link = (String)p;
            // Bitmap bitmap = ImageLoader.loadBitmap(currentPicturePath, null, screenSize.x, screenSize.y, true);

            i.close.setId(getChildCount()+20244202);
            //i.close.setOnClickListener(this);
            /*i.setOnTouchListener(new TelegraphEditor.OnSwipeTouchListener(context) {
                public void onSwipeTop() {
                 }
                public void onSwipeRight() {
                }
                public void onSwipeLeft(){
                }
                public void onSwipeBottom(){
                }
            });*/
            i.v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            TelegraphEditText.this.addView(i,LayoutHelper.MATCH_PARENT,AndroidUtilities.dp(250));
            //media.push(t);
            TgpEdit plus = new TgpEdit(c);
            plus.setHint("");
            plus.setId(getChildCount()+20244202);
            plus.setText("\n");
            TelegraphEditText.this.addView(plus);
        }else if(type  == 1){
            final EditText input = new EditText(((BaseFragment) delegate).getParentActivity());
           addVideo(new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int whic) {
                   String link = input.getText().toString();
                   PhotoView v = new PhotoView(TelegraphEditText.this.getContext(),1);
                   v.w.loadVideo(link,null,link,true);
                   v.close.setId(getChildCount()+20244202);
                    v.link = link;
                   TelegraphEditText.this.addView(v);
                   //media.push(t);
                   TgpEdit plus = new TgpEdit(getContext());
                   plus.setHint("");
                   plus.setId(getChildCount()+20244202);
                   TelegraphEditText.this.addView(plus);

               }
           },input);
        }
    }

    private  void addVideo(final DialogInterface.OnClickListener x, final EditText input){
        AlertDialog.Builder builder = new AlertDialog.Builder(((BaseFragment) delegate).getParentActivity());
        LinearLayout l = new LinearLayout(((BaseFragment) delegate).getParentActivity());
        l.setOrientation(LinearLayout.VERTICAL);

        //input.setPadding(60,60,60,60);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        //title.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
        input.setHint(ApplicationLoader.applicationContext.getString(R.string.LinkPrompt1));
        l.addView(input);
        builder.setView(l);
        builder.setPositiveButton("OK", x);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ((BaseFragment) delegate).showDialog(builder.create());

    }

    //static Stack<TImbed> media = new Stack<>();

    public List<Node> toHtml() {
        //simply process this object
        int size = getChildCount();
        ArrayList<Node> list = new ArrayList<>();
        int len = 0;
        for(int x = 0; x < size; x++){
            View v = getChildAt(x);
            if(v instanceof TgpEdit){
                TgpEdit t = (TgpEdit) v;
                Editable tt = t.getText();
                //ru.noties.markwon.spans.
                //split by new line add br
                //split by space
                String ff = tt.toString();
                len += ff.length();
                String newlit [] = ff.split("\n");
                int l = 0;
                for(int i = 0; i < newlit.length; i++){
                   if(i!=0) list.add(NodeElement.newInstance(l+newlit[i].length(),"br",null));
                    String spalit [] = newlit[i].split(" ");
                    for(int m =0; m < spalit.length; m++){
                        NodeText tx = new NodeText(spalit[m] + " ");
                        l +=  spalit[m].length() + 1;
                        tx.start = l;
                        list.add(tx);
                    }
                }

                ru.noties.markwon.spans.StrongEmphasisSpan [] ls = tt.getSpans(0,tt.length(),ru.noties.markwon.spans.StrongEmphasisSpan.class);
                for(int y =0; y < ls.length; y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls[y]),"b",null,tt.getSpanEnd(ls[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls[y]),"/b",null,tt.getSpanEnd(ls[y])));
                    Log.d("DickHeadi",tt.getSpanStart(ls[y])+","+tt.getSpanEnd(ls[y]));
                }
                URLSpan [] ls1 = tt.getSpans(0,tt.length(),URLSpan.class);
                for(int y =0; y < ls1.length; y++){
                    Map<String,String> m = new HashMap<>();
                    m.put("href",ls1[y].getURL());
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls1[y]),"a",m));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls1[y]),"/a",m));
                    Log.d("DickLinks "+ls1[y].getURL(),tt.getSpanStart(ls1[y])+","+tt.getSpanEnd(ls1[y]));
                }
                ru.noties.markwon.spans.EmphasisSpan [] ls2 = tt.getSpans(0,tt.length(),ru.noties.markwon.spans.EmphasisSpan.class);
                for(int y =0; y < ls2.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls2[y]),"i",null,tt.getSpanEnd(ls2[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls2[y]),"/i",null));

                    Log.d("DickEmp",tt.getSpanStart(ls2[y])+","+tt.getSpanEnd(ls2[y]));
                }

                ru.noties.markwon.spans.BlockQuoteSpan [] ls3 = tt.getSpans(0,tt.length(),ru.noties.markwon.spans.BlockQuoteSpan.class);
                for(int y =0; y < ls3.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls3[y]),"blockquote",null,tt.getSpanEnd(ls3[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls3[y]),"/blockquote",null));
                    Log.d("DickBQ",tt.getSpanStart(ls3[y])+","+tt.getSpanEnd(ls3[y]));
                }
                RelativeSizeSpan [] ls4 = tt.getSpans(0,tt.length(),RelativeSizeSpan.class);
                for(int y =0; y < ls4.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls4[y]),ls4[y].getSizeChange()>0?"h4":"h3",null,tt.getSpanEnd(ls4[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls4[y]),ls4[y].getSizeChange()>0?"/h4":"/h3",null));
                    Log.d("DickSrongEmpha",tt.getSpanStart(ls4[y])+","+tt.getSpanEnd(ls4[y]));
                }
                UnderlineSpan [] ls5 = tt.getSpans(0,tt.length(),UnderlineSpan.class);
                for(int y =0; y < ls5.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls5[y]),"u",null,tt.getSpanEnd(ls5[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls5[y]),"/u",null));
                    Log.d("DickUnderline",tt.getSpanStart(ls5[y])+","+tt.getSpanEnd(ls5[y]));
                }
                StrikethroughSpan [] ls6 = tt.getSpans(0,tt.length(),StrikethroughSpan.class);
                for(int y =0; y < ls6.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls6[y]),"s",null,tt.getSpanEnd(ls6[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls6[y]),"/s",null));

                    Log.d("DickStrikeThr",tt.getSpanStart(ls6[y])+","+tt.getSpanEnd(ls6[y]));
                }
                StyleSpan [] ls7 = tt.getSpans(0,tt.length(),StyleSpan.class);
                for(int y =0; y < ls7.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls7[y]),ls7[y].getStyle()==Typeface.ITALIC?"i":"b",null,tt.getSpanEnd(ls7[y])));
                    list.add(NodeElement.newInstance(tt.getSpanEnd(ls7[y]),ls7[y].getStyle()==Typeface.ITALIC?"/b":"b",null));
                    Log.d("Style "+ls7[y].getStyle(),tt.getSpanStart(ls7[y])+","+tt.getSpanEnd(ls7[y]));
                }
                LeadingMarginSpan [] ls8 = tt.getSpans(0,tt.length(),ThematicBreakSpan.class);
                for(int y =0; y < ls8.length;y++){
                    list.add(NodeElement.newInstance(tt.getSpanStart(ls8[y]),"hr",null));

                    Log.d("DicLeark "+ls8[y].getLeadingMargin(true),tt.getSpanStart(ls8[y])+","+tt.getSpanEnd(ls8[y]));
                }
            }else if(v instanceof PhotoView){
                PhotoView b = (PhotoView) v;
                if(b.w == null){
                    Map<String,String> bb = new HashMap<>();
                    bb.put("src", b.link);
                    list.add(NodeElement.newInstance(len,"img",bb));
                }else {
                    Map<String,String> bb = new HashMap<>();
                    bb.put("src", b.link);
                    list.add(NodeElement.newInstance(len,"video",bb));
                }
            }
        }

        //build node children
        //sort then print
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(o1.start > o2.start) return 1;
                else if(o1.start < o2.start) return -1;
                return 0;
            }
        });

        ArrayList<Node> temp = new ArrayList<Node>();
        NodeElement tn = null;
        for(int l = 0; l < list.size(); l++){
            Log.d("NODE",list.get(l).toString()+","+list.get(l).start);
            Node c = list.get(l);
            if(c instanceof NodeElement){
                NodeElement el = (NodeElement) c;
                
                if(el.getTag().startsWith("/")){
                    if(tn!=null && tn.getChildren().size()>0)temp.add(tn);
                     tn = null;
                }else if(!el.getTag().equals("br") && !el.getTag().equals("hr")) {
                   tn = el;
                    if(tn.getChildren()==null){
                       tn.setChildren(new ArrayList<Node>());
                    }
                    if(el.getTag().equals("blockquote"))temp.add(NodeElement.newInstance(el.start-1,"br",null));

                }else {
                     temp.add(c);
                }
            }else  {
                if(tn!=null){
                    if(tn.getChildren()==null){
                        tn.setChildren(new ArrayList<Node>());
                    }
                    tn.getChildren().add(c);
                }
                else{  temp.add(c);}
            }
        }

        for(int l = 0; l < temp.size(); l++){
            Log.d("NODE1",temp.get(l).toString()+"");
        }


        return temp;
    }
    public static Editable fromHtml(String html) {
        //simply process this object

        return null;
    }
    class TgpEdit extends EditText {
        @Override
        public boolean getFreezesText() {
            return true;
        }

        @Override
        protected boolean getDefaultEditable() {
            return true;
        }


        @Override
        protected MovementMethod getDefaultMovementMethod() {
            return ArrowKeyMovementMethod.getInstance();
        }

        public void anchor() {
            fromSelection = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(((BaseFragment) delegate).getParentActivity());
            //builder.setTitle("Input Anchor");
            LinearLayout l = new LinearLayout(((BaseFragment) delegate).getParentActivity());
            l.setOrientation(LinearLayout.VERTICAL);
            final EditText input = new EditText(((BaseFragment) delegate).getParentActivity());
            final EditText alt = new EditText(((BaseFragment) delegate).getParentActivity());

            alt.setInputType(InputType.TYPE_CLASS_TEXT);
            alt.setHint(ApplicationLoader.applicationContext.getString(R.string.AnchorText));
            //input.setPadding(60,60,60,60);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            //title.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
            input.setHint(ApplicationLoader.applicationContext.getString(R.string.LinkPrompt));
            l.addView(alt);
            l.addView(input);
            builder.setView(l);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whic) {
                    String link = input.getText().toString();
                    String altt = alt.getText().toString();
                    Log.d("sfsd", altt + ":" + link);

                    if (altt.length() > 0) {
                        //getMd(R.id.link, " [" + altt + "](" + link + ")")
                        int len = getText().length();
                        getText().append(altt);
                        getText().setSpan(new URLSpan(link),len, len+altt.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            ((BaseFragment) delegate).showDialog(builder.create());
            //link.setActivated(!link.isActivated());
        }

        int currentX;
        int currentY;


               /* @Override
                public boolean dispatchKeyEvent(KeyEvent e){
                    Log.w("ZZZ",""+e.toString());
                    return super.dispatchKeyEvent(e);
                }

                @Override
                public boolean onKeyPreIme(int ke,KeyEvent s){
                    Log.w("ZZZ",""+ke);
                    return super.onKeyPreIme(ke,s);
                }*/

        public TgpEdit(Context context) {
            super(context);

            init(context, null);
            setMaxLines(Integer.MAX_VALUE);
            setOnClickListener(TelegraphEditText.this);
            //setOnKeyListener(TelegraphEditText.this);
            //ed.setPointerIcon(PointerIcon.getSystemIcon(context,PointerIcon.TYPE_HORIZONTAL_DOUBLE_ARROW));
            setGravity(Gravity.TOP);
            setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(15), AndroidUtilities.dp(10), AndroidUtilities.dp(15));
            //ed.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP));
            setHint(ApplicationLoader.applicationContext.getString(R.string.TgpBody));
            setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    currentX = (int) event.getAxisValue(MotionEvent.AXIS_X);
                    currentY = (int) event.getAxisValue(MotionEvent.AXIS_Y);
                    return false;
                }
            });
            final BetterPopupWindow dw = new BetterPopupWindow(this, R.layout.options);
            dw.setEditHandler(new BetterPopupWindow.OnEditDelegate() {
                @Override
                public void onButtonPressed(int which) {
                    selectionStart = getSelectionStart();
                    selectionEnd = getSelectionEnd();
                    TgpEdit.this.onButtonPressed(which);
                }

                @Override
                public void onClear(int which) {
                    selectionStart = getSelectionStart();
                    selectionEnd = getSelectionEnd();
                    TgpEdit.this.onClear(which);
                }

                @Override
                public Object[] selectionSpans() {
                    return TgpEdit.this.getText().getSpans(selectionStart, selectionEnd, Object.class);
                }
            });
            setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);
            setInputType(getInputType() | EditorInfo.IME_NULL);

            setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int mult = (int) getTextSize();
                    //boolean key = AndroidUtilities.isAccepting();
                    final android.graphics.Rect rect = new android.graphics.Rect();
                    if (getSelectionEnd() > 0) {
                        dw.showLikeQuickAction(currentX + mult, currentY + mult * 4);
                        delegate.longClicked(true);
                    } else {
                        delegate.longClicked(false);
                    }
                    v.getWindowVisibleDisplayFrame(rect);
                    int usableViewHeight = v.getHeight() - (rect.top != 0 ? AndroidUtilities.statusBarHeight : 0) - AndroidUtilities.getViewInset(v);
                    int keyboardHeight = usableViewHeight - (rect.bottom - rect.top);
                    if (keyboardHeight > 0) {
                        delegate.keyboardAttached(keyboardHeight);
                    }
                    return false;
                }
            });
            addTextChangedListener(new TextWatcher() {
                int prev;
                int start;
                int len;
                boolean skip;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    prev = s.length();


                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    edlen = s.length();

                    if (prev == 0 && edlen > 0 && delegate.titlen() > 0) {
                        if (!delegate.isActionModeShown()) {
                            delegate.onRequirementChange(true);
                        }
                    } else if (edlen == 0) {
                        delegate.onRequirementChange(false);
                    }
                    this.start = start;
                    this.len = start + count;


                }
                int bstart;
                @Override
                public void afterTextChanged(Editable s) {
                    if (len - start > 0) {
                        //if(state==4){
                          //  bstart = start;
                        //}
                        if (tstate != 0 || state > 0) {
                            if (fromSelection) {
                                fromSelection = false;
                                return;
                            }
                            if (!skip) {
                                CharSequence subs = s.subSequence(start, len);
                                String now = subs.toString();
                                if (now.trim().isEmpty()) {
                                    if (tstate != 4 && tstate != 5 && tstate != 6 && tstate != 7){
                                        state = tstate;
                                    }
                                    else if (now.contains("\n")) {
                                        state = tstate;
                                    }
                                    return;
                                }
                                skip = true;
                                Log.d("DDD", now + "XX" + state + "," + getMeasuredWidth());
                                s.replace(start,len, getStatefulMd(now));
                                if ((state != 0)) {//(state == 8 || state == 16 || state == 4 || state==128 || state==64 || state==1 || state==2) {
                                    tstate = state;
                                    state = 0;
                                }
                            } else skip = false;
                        } else {
                            //unknown dot bug fix
                            String now = s.subSequence(this.start, len).toString();
                            if (". ".equals(now)) {
                                skip = true;
                                s.replace(start, len, "  ");
                                if ((state != 0)) {//(state == 8 || state == 16 || state == 4 || state==128 || state==64 || state==1 || state==2) {
                                    tstate = state;
                                    state = 0;
                                }
                            }
                        }
                    }
                }
            });

            setBackground(null);
        }


        private void style(Editable s,int start,int len){

        }

        private void reset(){

        }

        public TgpEdit(Context context, AttributeSet attrs) {
            super(context, attrs);

            init(context, attrs);
        }

        private CharSequence getStatefulMd(String button) {
            if (". ".equals(button)) return " ";
            switch (state) {

                case 1:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "**" + button + "**");
                case 2:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "*" + button + "*");
                    case 3:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "<i><b>" + button + "</b></i>");
                case 4:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "\n>" + button + "");
                    case 5:  return Markwon.markdown(ApplicationLoader.applicationContext, "\n>**" + button + "**");
                    case 6:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "\n>*" + button + "*");
                    case 7:
                    break;
                case 8:
                    return new SpannableRenderer().render(SpannableConfiguration.builder(ApplicationLoader.applicationContext).build(), Markwon.createParser().parse("<h4>" + button + "</h4>"));
                case 16:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "<h1>" + button + "</h1>");
                case 32:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "`" + button + "`");
                case 36:
                    return getMd(R.id.quote, "`" + button + "`");
                case 64:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "<u>" + button + "</u>");
                case 128:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "<s>" + button + "</s>");
                case 186:
                    return Markwon.markdown(ApplicationLoader.applicationContext, "<s><u>" + button + "</u></s>");


            }
            switch (tstate){
                case 5:  return Markwon.markdown(ApplicationLoader.applicationContext, "**" + button + "**");
                case 6:  return Markwon.markdown(ApplicationLoader.applicationContext, "*" + button + "*");
            }
            return button;
        }

        private void init(Context context, AttributeSet attributeSet) {
            //helper = MarkwonViewHelper.create(this);
            //helper.init(context, attributeSet);
        }

        int selectionStart;
        int selectionEnd;

        private void onButtonPressed(final int which) {
            try {
                final Editable str = getText();
                final CharSequence selected = str.subSequence(selectionStart, selectionEnd);
                if (which == R.id.quote) {
                    String b = getText().toString();
                    selectionStart = b.substring(0, selectionStart).lastIndexOf("\n");
                    if (selectionStart == -1) selectionStart = 0;
                    if (str.getSpans(selectionStart + 1, selectionEnd, BlockQuoteSpan.class).length > 0)
                        return;
                }
                if (which == R.id.image) {
                    insertAt(0, selectionStart, 1, TelegraphEditText.this.indexOfChild(TgpEdit.this));
                } else if (which == R.id.video) {
                    //
                    final EditText input = new EditText(((BaseFragment) delegate).getParentActivity());
                    addVideo(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whic) {
                            String link = input.getText().toString();
                            insertAt(1, selectionStart, link, TelegraphEditText.this.indexOfChild(TgpEdit.this));
                        }
                    }, input);
                } else if (R.id.clear == which) {
                    toHtml();
                } else if (R.id.link == which) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(((BaseFragment) delegate).getParentActivity());
                    //builder.setTitle("Input Anchor");
                    final EditText input = new EditText(((BaseFragment) delegate).getParentActivity());
                    //input.setPadding(60,60,60,60);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    //title.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
                    input.setHint(ApplicationLoader.applicationContext.getString(R.string.LinkPrompt));
                    builder.setView(input);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whic) {
                            String link = input.getText().toString();
                            str.setSpan(new URLSpan(link), selectionStart, selectionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    ((BaseFragment) delegate).showDialog(builder.create());
                } else if (which == R.id.horiz) {
                    str.append(getMd(which, selected));
                } else {
                    str.replace(selectionStart, selectionEnd, getMd(which, selected));
                }
                fromSelection = true;
            }catch (Exception s ){

            }
        }

        public boolean fromSelection;

            private void onClear(final int which) {
                final Editable str = getText();

                final CharSequence selected = str.subSequence(selectionStart, selectionEnd);
                str.replace(selectionStart, selectionEnd, selected.toString());

                fromSelection =true;
             }

                private CharSequence getMd(int which, CharSequence str) {
                    if(str.length()==0) if(which == R.id.horiz) {
                        SpannableString s = new SpannableString("\n ");
                        s.setSpan(new ThematicBreakSpan(SpannableTheme.create(getContext())),1,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return s;
                    } else return str;
                    switch (which) {
                        case R.id.bold:
                            str = "**" + str + "**";
                            break;
                        case R.id.italic:
                            str = "*" + str + "*";
                            break;
                        case R.id.quote:
                            str = "\n\n>" + str + "";
                            break;
                        case R.id.link:
                            break;
                        /*case R.id.singlequote:
                            str = "`" + str + "`";
                            break;*/
                        case R.id.size1:
                            str = "<h1>" + str + "</h1>";
                            break;
                        case R.id.size2:
                            str = "<h4>" + str + "</h4>";
                            break;
                        case R.id.horiz: str = "<hr/>"; break;
                        case R.id.underline: str = "<u>" + str+"</u>"; break;
                        case R.id.strike: str = "<s>" +str+ "</s>"; break;
                        case R.id.image:
                            str = "![" + str + "](file://" + str + ")";
                            break;
                        default:
                            return str;
                    }
                    return Markwon.markdown(ApplicationLoader.applicationContext, (String) str);
                }

                //@Override
                //public void setConfigurationProvider(@NonNull IMarkwonView.ConfigurationProvider provider) {
                //  helper.setConfigurationProvider(provider);
                //}

                //public void setMarkdown(@Nullable String markdown) {

                //helper.setMarkdown(markdown);
                //}

                public void setMarkdown(@Nullable SpannableConfiguration configuration, @Nullable String markdown) {
                    //helper.setMarkdown(configuration, markdown);
                }

                // @Nullable
                //@Override
                //public String getMarkdown() {
                //  return helper.getMarkdown();
                //}
    }
    int state;
    public int tstate;
    //transient
    public void tempState(int i) {
        tstate = i;
    }

    public void state(int i) {
        if(state+i<0)state = 0;
        state += i;
    }
    public void anchor(){
        try {
            TgpEdit g = null;
            Log.d("df", getChildCount() + "" + current);
            if (current == 0 || getChildCount() == 1) {
                g = (TgpEdit) getChildAt(0);
            } else g = findViewById(current);
            g.anchor();

        }catch (Exception d){
            Log.e("EEE",d.toString());
        }

    }

    public void horiz(){
        try {
            TgpEdit g = null;
            if (current == 0 || getChildCount() == 1) {
                g = (TgpEdit) getChildAt(0);
            } else g = findViewById(current);
            g.onButtonPressed(R.id.horiz);

        }catch (Exception d){
            Log.e("EEE",d.toString());
        }
    }
}
