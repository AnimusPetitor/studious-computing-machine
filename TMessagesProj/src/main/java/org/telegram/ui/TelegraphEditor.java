/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;

import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.googlecode.mp4parser.authoring.Edit;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;

import org.telegram.tgnet.Node;
import org.telegram.tgnet.NodeElement;
import org.telegram.tgnet.NodeText;
import org.telegram.tgnet.Page;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.TRequest;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;

import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;


import org.telegram.ui.Components.Paint.Input;
import org.telegram.ui.Components.TelegraphEditText;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.spans.SpannableTheme;
import ru.noties.markwon.spans.ThematicBreakSpan;


public class TelegraphEditor extends BaseFragment  implements TelegraphEditText.OnLongClick{

    public interface TelegraphEditorDelegate {
        void commitCurrentToRecents(TelegraphEditor activity, String title, List<Node> s, boolean draftf);
    }
    ChatActivity x;

    public TelegraphEditor(ChatActivity s){
        super();
        x = s;
    }
    Activity l;
    public TelegraphEditor(Activity s){
        super();
        l = s;
    }
    String titlet;
    String body;
    public TelegraphEditor(Activity s,String e,String title){
        super();
        body = e;
        l = s;
        titlet = title;

    }

    Editable b = new Editable.Factory().newEditable("");
    boolean edit;
    public TelegraphEditor(Activity s, Page bod, String title,boolean edit){
        super();
        this.edit = edit;
        //build editable
        if(bod!=null) {
            List<Node> body = bod.getContent();
            for (int x = 0; x < body.size(); x++) {
                Node n = body.get(x);
                if (n instanceof NodeText) {
                    NodeText t = (NodeText) n;
                    b.append(t.getContent());
                } else {
                    NodeElement m = (NodeElement) n;
                    Log.d("DD", m + "");
                    if (m.getTag().equals("blockquote")) {
                        List<Node> l = m.getChildren();
                        for (Node c : l) {
                            if (c instanceof NodeText) {
                                b.append(((NodeText) c).getContent());
                            } else {
                                NodeElement e = ((NodeElement) c);
                                List<Node> list = (List<Node>) e.getChildren();
                                if (list != null) {
                                    NodeText t = (NodeText) list.get(0);
                                    b.append(t.getContent());
                                    setSpan(e, t.getContent().length());
                                } else setSpan(e, -1);
                            }
                        }
                    } else {
                        List<Node> list = (List<Node>) m.getChildren();
                        if (list != null) {
                            NodeText t = (NodeText) list.get(0);
                            b.append(t.getContent());
                            setSpan(m, t.getContent().length());
                        } else setSpan(m, -1);
                    }
                }
            }
            l = s;
            titlet = title;
        }
    }



    private void setSpan(NodeElement n,int l){
        try {
            String tag = n.getTag();
            n.start = b.length() - l; n.end = n.start + l;
            Log.d("DD",tag+""+n.start+","+n.end);
            switch (tag) {
                case "b":
                    b.setSpan(new ru.noties.markwon.spans.StrongEmphasisSpan(), n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "a":
                    URLSpan url = new URLSpan(n.getAttrs().get("href"));
                    b.setSpan(url, n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "i":
                    b.setSpan(new StyleSpan(Typeface.ITALIC), n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "blockquote":
                    b.setSpan(new ru.noties.markwon.spans.BlockQuoteSpan(SpannableTheme.create(getParentActivity())), n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "h4":
                    b.setSpan(new RelativeSizeSpan(1.7f), n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "h3":
                    b.setSpan(new RelativeSizeSpan(1.25f), n.start, n.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "u":
                    b.setSpan(new UnderlineSpan(),n.start,n.end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "s":
                    b.setSpan(new StrikethroughSpan(),n.start,n.end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "hr":
                    SpannableString s = new SpannableString("\n ");
                    s.setSpan(new ThematicBreakSpan(SpannableTheme.create(ApplicationLoader.applicationContext)),1,2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    b.append(s);
                    break;
                case "br": b.append("\n");
                    break;
                case "img": Log.d("IMg",n+""); break;
            }
        }catch (Exception s){
            Log.d("setSpan",s.toString());
        }
    }

    private NumberTextView selectedMessagesCountTextView;
    private TelegraphEditorDelegate delegate;
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private final static int done = 3;
    private boolean evisible = true;

    public void  onRequestImage(PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate p){
        if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
            return ;
        }
        PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(false, true, true,  x);
        fragment.setDelegate(p);
        presentFragment(fragment);
    }

    @Override
    public void longClicked(boolean show) {
        evisible = show;
        btnPressed();
    }

    @Override
    public void keyboardAttached(int keyboardHeight) {
        vx.setTranslationY(keyboardHeight);
    }

    @Override
    public void onRequirementChange(boolean to) {
        if(to)actionBar.showActionMode();
        else actionBar.hideActionMode();
    }

    @Override
    public boolean isActionModeShown() {
        return actionMode.isShown();
    }

    @Override
    public int titlen() {
        return titlen;
    }

    @Override
    public boolean onFragmentCreate() {
        //loadRecentFiles();
        return super.onFragmentCreate();
    }
    LinearLayout vh;
    LinearLayout xl;
    ImageView btn;
    TelegraphEditText ed;
    HorizontalScrollView vx;
    EditText title;
    public int titlen;
    ActionBarMenu actionMode;
    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }
    boolean editorAct = false;
    @Override
    public View createView(final Context context) {
        //UI_tweak_telegraph
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("SelectFile", R.string.SelectFile));//UI_tweak_telegraph string
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (actionBar.isActionModeShowed()) {
                        actionBar.hideActionMode();
                    } else {
                        //if(ed.getText().length()>0)
                        if(!edit)delegate.commitCurrentToRecents(TelegraphEditor.this,title.getText().toString(),ed.toHtml(), true);
                        finishFragment();
                        //delegate.commitCurrentToRecents(TelegraphEditor.this,title.getText().toString(),ed.getText());
                    }
                } else if (id == done) {
                    //read history credentials autocomplete
                    FileInputStream fi = null;
                    String [] list = null;
                    try {
                        fi = new FileInputStream(Environment.getExternalStorageDirectory() + "/Telegram/.last");
                        ObjectInputStream oo = new ObjectInputStream(fi );
                         list = (String[]) oo.readObject();
                    } catch (Exception e) {
                        Log.d("EE",e.toString());
                    }


                    if (delegate != null) {
                        AndroidUtilities.hideKeyboard(ed);
                        /**/
                        if(AndroidUtilities.isConnected(getParentActivity())) {
                            final EditText input = new EditText(getParentActivity());
                            final EditText alt = new EditText(getParentActivity());
                            if(list!=null){
                                input.setText(list[1]);
                                alt.setText(list[0]);
                            }
                            add(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String titl = title.getText().toString();
                                    final List<Node> nodes = ed.toHtml();
                                    String url = input.getText().toString();
                                    Log.d("Shave", nodes + "" + url);
                                    if (url != null && !url.startsWith("http"))
                                        url = "http://" + url;
                                    final AlertDialog progressDialog  = new AlertDialog(context, 1);
                                    if (context != null) {
                                        try {
                                            progressDialog.setMessage("Publishing...");
                                            progressDialog.setCanceledOnTouchOutside(false);
                                            progressDialog.setCancelable(true);
                                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialog) {

                                                }
                                            });
                                            progressDialog.show();
                                        } catch (Exception x) {
                                            FileLog.e(x);
                                        }
                                    }
                                    TRequest.shave(nodes, titl, alt.getText().toString(), url, new TRequest.OnPublished() {
                                        @Override
                                        public void onPublished(Page e) {
                                            progressDialog.dismiss();
                                            if(e!=null){
                                                delegate.commitCurrentToRecents(TelegraphEditor.this, titl, nodes, false);
                                            }else {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                final TextView input = new TextView(context);
                                                input.setText("Invalid url!");
                                                input.setTextSize(23);
                                                input.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT,Gravity.CENTER));
                                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                                input.setHint(ApplicationLoader.applicationContext.getString(R.string.LinkPrompt));
                                                builder.setView(input);
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int whic) {
                                                       dialog.dismiss();
                                                    }
                                                });
                                                showDialog(builder.create());
                                            }

                                        }
                                    });

                                }
                            }, alt, input);
                        }else{

                        }

                    }
                }
            }
        });

        actionModeViews.clear();
        actionMode = actionBar.createActionMode();

        if(Theme.usePlusTheme)actionMode.setBackgroundColor(Theme.chatHeaderColor);

        selectedMessagesCountTextView = new NumberTextView(actionMode.getContext());
        selectedMessagesCountTextView.setTextSize(18);
        selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        selectedMessagesCountTextView.setTextColor(Theme.usePlusTheme ? Theme.chatHeaderIconsColor : Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        selectedMessagesCountTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });
        selectedMessagesCountTextView.setVisibility(View.INVISIBLE);
        actionMode.addView(selectedMessagesCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 65, 0, 0, 0));


        actionModeViews.add(actionMode.addItemWithWidth(done, R.drawable.ic_ab_done, AndroidUtilities.dp(54)));

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;


        /*888888888888888888888888888888888888888888888888888888888888888888888888888888888*/
        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        ed = new TelegraphEditText(context);

        title = new EditText(context);
        title.setPadding(AndroidUtilities.dp(15),AndroidUtilities.dp(12),10,AndroidUtilities.dp(10));
        //title.setPointerIcon(PointerIcon.getSystemIcon(PointerIcon.TYPE_CROSSHAIR));
        title.setTextSize(24);
       // title.setPadding(5,0, 0,0);
        //title.setShadowLayer(2,1,1,Theme.darkColor);
        title.setSingleLine(false);
        title.setMaxLines(4);
        if(titlet!=null)title.setText(titlet);
        title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("ZZZ",actionId+"");
                return false;
            }
        });

        title.setTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
        //title.setHintColor(Theme.getColor(Theme.key_chat_messagePanelHint));
        title.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));
        title.setContentDescription("Title");
        //title.setBackground(null);
        title.setBackground(Theme.getThemedDrawable(context,R.drawable.greydivider,Theme.key_windowBackgroundGrayShadow));
        title.setMinHeight(30);
        title.setHint(ApplicationLoader.applicationContext.getString(R.string.TgpTitle));
        title.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));

        //title.getBackground().clearColorFilter();
        //title.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
        LayoutInflater inf = (LayoutInflater) ApplicationLoader.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vx = (HorizontalScrollView) inf.inflate(R.layout.popup,null);
        final ImageButton bold = vx.findViewById(R.id.bold);
        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = bold.isActivated();
                bold.setActivated(!state);
                if(!state){
                    ed.tempState(1+ed.tstate);
                }else ed.tempState(0);
                ed.state(state ? - 1 :  1);
		
             }
        });

        final ImageButton underline = vx.findViewById(R.id.underline);
        underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = underline.isActivated();
                underline.setActivated(!state);
                if(!state){
                    ed.tempState(64+ed.tstate);
                }else ed.tempState(0);
                ed.state(state ? - 64 :  64);

            }
        });



        final ImageButton strike = vx.findViewById(R.id.strike);
        strike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = strike.isActivated();
                strike.setActivated(!state);
                if(!state){
                    ed.tempState(128+ed.tstate);
                }else ed.tempState(0);
                ed.state(state ? - 128:  128);

            }
        });

        final ImageButton ital = vx.findViewById(R.id.italic);
        ital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = ital.isActivated();
		        ital.setActivated(!state);
                if(!state){
                    ed.tempState(2+ed.tstate);
                }else ed.tempState(0);
                ed.state( state ? -2 : 2);
                
             }
        });

        final ImageButton size1 = vx.findViewById(R.id.size1);
        final ImageButton size2 = vx.findViewById(R.id.size2);
        size1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = size1.isActivated();
		        size1.setActivated(!state);
	        	if(!state){
	        	    ed.tempState(16);
	        	    if(size2.isActivated())size2.performClick();
	        	}else ed.tempState(0);
              ed.state( state ?  - 16 : 16);
                
              }
        });

        size2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = size2.isActivated();
		        size2.setActivated(!state);
		        if(!state){
		           ed.tempState(8);
		            if(size1.isActivated())size1.performClick();
		        }else ed.tempState(0);
                ed.state(state ? - 8 :  8);
                
             }
        });

        final ImageButton link = vx.findViewById(R.id.link);
        link.setOnClickListener(new View.OnClickListener() {
            //TelegraphEditor.this.fromSelection = true;
            @Override
            public void onClick(View v) {
                ed.anchor();
            }
        });

        final ImageButton hr = vx.findViewById(R.id.horiz);
        hr.setOnClickListener(new View.OnClickListener() {
            //TelegraphEditor.this.fromSelection = true;
            @Override
            public void onClick(View v) {
               ed.horiz();

            }
        });

        final ImageButton image = vx.findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRequestImage(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
                    @Override
                    public void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<Integer> ttls, ArrayList<MediaController.PhotoEntry> videos, ArrayList<ArrayList<TLRPC.InputDocument>> masks, ArrayList<MediaController.SearchImage> webPhotos) {
                        for (int a = 0; a < photos.size(); a++) {
                            String video = photos.get(a);
                            Log.d("fff",video+"");
                            if (video != null) {
                                if(ed.getChildCount()==1){
                                    TextView tv =  ((TextView)ed.getChildAt(0));
                                    if(tv.getText().length()==0)tv.setHint("");
                                }
                                ed.add(0,video,context);

                            }
                        }
                    }

                    @Override
                    public void startPhotoSelectActivity() {
                        try {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            Intent chooserIntent = Intent.createChooser(photoPickerIntent, null);
                            startActivityForResult(chooserIntent, 100);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                });
             }
        });

        final ImageButton video = vx.findViewById(R.id.video);
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ed.getChildCount()==1){
                    TextView tv =  ((TextView)ed.getChildAt(0));
                    if(tv.getText().length()==0)tv.setHint("");
                }
		        ed.add(1,"",context);
                video.setActivated(!video.isActivated());
             }
        });


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vh = new LinearLayout(context);
        vh.setOrientation(LinearLayout.VERTICAL);

        btn = new ImageView(context);
        btn.setColorFilter(Theme.usePlusTheme ? Theme.chatQuickBarNamesColor != 0xff212121 ? Theme.chatQuickBarNamesColor : Theme.defColor : Theme.getColor(Theme.key_chat_goDownButtonIcon), PorterDuff.Mode.SRC_IN);
        btn.setImageResource(R.drawable.search_down);
        btn.setScaleType(ImageView.ScaleType.CENTER);

        Drawable d = context.getResources().getDrawable(R.drawable.ic_bar_bg_f);
        d.setColorFilter(Theme.usePlusTheme ? Theme.chatQuickBarColor : Theme.getColor(Theme.key_chat_goDownButton), PorterDuff.Mode.MULTIPLY);
        btn.setBackgroundDrawable(d);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               btnPressed();
            }

        });

        btn.setOnTouchListener(new OnSwipeTouchListener(context) {
            public void onSwipeTop() {
                btnPressed();
            }
            public void onSwipeRight() {
            }
            public void onSwipeLeft(){
            }
            public void onSwipeBottom() {
                btnPressed();
            }
        });

        title.addTextChangedListener(new TextWatcher() {
            int prev;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {prev = s.length();}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titlen = s.length();
                if(prev == 0 && titlen > 0 && TelegraphEditText.edlen > 0) {
                    if(!actionMode.isShown()){
                       actionBar.showActionMode();
                    }
                }else if(titlen==0){
                    actionBar.hideActionMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        ed.setLongDelegate(this);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        vh.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams bl = LayoutHelper.createFrame(-2, -2, Gravity.TOP,20,0,0,0);

        vh.addView(btn,bl);
        vh.addView(vx);

         RelativeLayout edvh = new RelativeLayout(context);
         //xl = new LinearLayout(context);
         //xl.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP));
         //xl.setOrientation(LinearLayout.VERTICAL);
         //xl.addView(ed);
         ScrollView edh = new ScrollView(context);

         if(this.body!=null){
             ed.setText(body);
         }else if(this.b!=null){
             ed.setText(this.b);
         }
         edh.addView(ed);
         edvh.addView(edh,LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.CENTER));
         edvh.addView(vh);

        l.addView(title);
        l.addView(edvh);
        /*888888888888888888888888888888888888888888888888888888888888888888888888888888888*/
        frameLayout.addView(l, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.CENTER));
        actionBar.setTitle("New Article");
        return fragmentView;
    }

    private void btnPressed(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(vh,  "translationY", evisible ?  vx.getHeight() : 0).setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btn.setImageResource(!evisible ?  R.drawable.search_down : R.drawable.search_up);
                evisible = !evisible;
            }
        });
        animator.start();

    }

    private static int getSize(float size) {
        return (int) (size < 0 ? size : AndroidUtilities.dp(size));
    }

    @Override
    public void onResume() {
        super.onResume();

        fixLayoutInternal();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AndroidUtilities.hideKeyboard(ed);
    }

    private void fixLayoutInternal() {
        if (selectedMessagesCountTextView == null) {
            return;
        }
        if (!AndroidUtilities.isTablet() && ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            selectedMessagesCountTextView.setTextSize(18);
        } else {
            selectedMessagesCountTextView.setTextSize(20);
        }
    }

    @Override
    public boolean onBackPressed() {

        return super.onBackPressed();
    }




    public void setDelegate(TelegraphEditorDelegate delegate) {
        this.delegate = delegate;
    }


    private void showErrorBox(String error) {
        if (getParentActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", R.string.AppName)).setMessage(error).setPositiveButton(LocaleController.getString("OK", R.string.OK), null).show();
    }


    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarActionModeDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, null, null, null, null, Theme.key_actionBarActionModeDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, null, null, null, null, Theme.key_actionBarActionModeDefaultTop),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarActionModeDefaultSelector)
        };
    }
    class OnSwipeTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;
        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new OnSwipeTouchListener.GestureListener());
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_THRESHOLD = /*100*/10;
            private static final int SWIPE_VELOCITY_THRESHOLD = /*100*/10;
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
                result = true;
                return result;
            }
        }
        public void onSwipeRight() {}
        public void onSwipeLeft() {}
        public void onSwipeTop() {}
        public void onSwipeBottom() {}
    }

    private  void add(final DialogInterface.OnClickListener x, final EditText alt, final EditText input){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        //builder.setTitle("Input Anchor");
        LinearLayout l = new LinearLayout(getParentActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        alt.setInputType(InputType.TYPE_CLASS_TEXT);
        alt.setHint(ApplicationLoader.applicationContext.getString(R.string.author)); //autocomplete

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        //title.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelText));
        input.setHint(ApplicationLoader.applicationContext.getString(R.string.authorlink));
        l.addView(alt);
        l.addView(input);
        builder.setView(l);

        builder.setPositiveButton("OK", x);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        showDialog(builder.create());

    }
}
