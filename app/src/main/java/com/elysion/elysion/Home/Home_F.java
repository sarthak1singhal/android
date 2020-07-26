package com.elysion.elysion.Home;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlWatermarkFilter;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.elysion.elysion.Comments.Comment_F;
import com.elysion.elysion.Main_Menu.MainMenuActivity;
import com.elysion.elysion.Main_Menu.MainMenuFragment;
import com.elysion.elysion.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.elysion.elysion.Profile.Profile_F;
import com.elysion.elysion.R;
import com.elysion.elysion.SimpleClasses.API_CallBack;
import com.elysion.elysion.SimpleClasses.ApiRequest;
import com.elysion.elysion.SimpleClasses.Callback;
import com.elysion.elysion.SimpleClasses.Fragment_Callback;
import com.elysion.elysion.SimpleClasses.Fragment_Data_Send;
import com.elysion.elysion.SimpleClasses.Functions;
import com.elysion.elysion.SimpleClasses.SaveVideoBg;
import com.elysion.elysion.SimpleClasses.TicTic;
import com.elysion.elysion.SimpleClasses.Variables;
import com.elysion.elysion.SoundLists.VideoSound_A;
import com.elysion.elysion.Taged.Taged_Videos_F;
import com.elysion.elysion.VideoAction.VideoAction_F;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */

// this is the main view which is show all  the video in list
public class Home_F extends RootFragment  implements Player.EventListener, Fragment_Data_Send, CacheListener {

    View view;
    Context context;


    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list =new ArrayList<>();;
    int currentPage=-1;
    LinearLayoutManager layoutManager;

    ProgressBar p_bar;
    SwipeRefreshLayout swiperefresh;

    boolean is_user_stop_video=false;
    public Home_F() {
        // Required empty public constructor
    }

    int swipe_count=0;
    @Override
    public void onCacheAvailable(File file, String url, int percentsAvailable) {
      //  progressBar.setSecondaryProgress(percentsAvailable);
       // setCachedState(percentsAvailable == 100);
     }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_home, container, false);
        context=getContext();

        p_bar=view.findViewById(R.id.p_bar);

        recyclerView=view.findViewById(R.id.recylerview);
        layoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper =  new PagerSnapHelper();
         snapHelper.attachToRecyclerView(recyclerView);



        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                 final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                  final int height = recyclerView.getHeight();
                int page_no=scrollOffset / height;
              //  if (dy<4 && dy>0)
                 //   OpenProfile(data_list.get(page_no),true);
               //else
                   if(page_no!=currentPage ){
                    currentPage=page_no;

                    Release_Privious_Player();
                    Set_Player(currentPage);

                }
            }
        });



        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setProgressViewOffset(false, 0, 200);

        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage=-1;
                Call_Api_For_get_Allvideos();
            }
        });

        Call_Api_For_get_Allvideos();

        if(!Variables.is_remove_ads)
        Load_add();

        return view;
    }




    InterstitialAd mInterstitialAd;
    public void Load_add() {

        // this is test app id you will get the actual id when you add app in your
        //add mob account
        MobileAds.initialize(context,
                getResources().getString(R.string.ad_app_id));


        //code for intertial add
        mInterstitialAd = new InterstitialAd(context);

        //here we will get the add id keep in mind above id is app id and below Id is add Id
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.my_Interstitial_Add));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });


    }



    boolean is_add_show=false;
    Home_Adapter adapter;
    public void Set_Adapter(){

         adapter=new Home_Adapter(context, data_list, new Home_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final Home_Get_Set item, View view) {

                switch(view.getId()) {

                    case R.id.user_pic:
                        onPause();
                        OpenProfile(item,false);
                        break;

                    case R.id.username:
                        onPause();
                        OpenProfile(item,false);
                        break;

                    case R.id.like_layout:
                        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                        Like_Video(postion, item);
                        }else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.comment_layout:
                        OpenComment(item);
                        break;

                    case R.id.shared_layout:
                        if (!is_add_show && (mInterstitialAd!=null && mInterstitialAd.isLoaded())) {
                            mInterstitialAd.show();
                            is_add_show = true;
                        } else {
                             is_add_show = false;
                                final VideoAction_F fragment = new VideoAction_F(item.video_id, new Fragment_Callback() {
                                    @Override
                                    public void Responce(Bundle bundle) {

                                        if (bundle.getString("action").equals("save")) {
                                            Save_Video(item);
                                        } else if (bundle.getString("action").equals("delete")) {
                                            Functions.Show_loader(context, false, false);
                                            Functions.Call_Api_For_Delete_Video(getActivity(), item.video_id, new API_CallBack() {
                                                    @Override
                                                    public void ArrayData(ArrayList arrayList) {

                                                    }

                                                    @Override
                                                    public void OnSuccess(String responce) {
                                                        data_list.remove(currentPage);
                                                        adapter.notifyDataSetChanged();

                                                    }

                                                    @Override
                                                    public void OnFail(String responce) {

                                                    }
                                                });

                                        }

                                    }
                                });

                                Bundle bundle = new Bundle();
                                bundle.putString("video_id", item.video_id);
                                bundle.putString("user_id", item.fb_id);
                                fragment.setArguments(bundle);
                                fragment.show(getChildFragmentManager(), "");
                            }

                        break;


                    case R.id.sound_name:
                        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                            if(check_permissions()) {
                                Intent intent = new Intent(getActivity(), VideoSound_A.class);
                                intent.putExtra("data", item);
                                startActivity(intent);
                            }
                        }else {
                            Toast.makeText(context, "Please Login.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                }

            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

    }



    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allvideos() {



        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("token",MainMenuActivity.token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(context, Variables.showAllVideos, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                swiperefresh.setRefreshing(false);
                Parse_data(resp);
            }
        });



    }
    HashMap<String,String> map=new HashMap<String, String>();//Creating HashMap

    public void Parse_data(String responce){

        int isAdapter = 0;
        if(data_list.size()!=0)
        {
            isAdapter = 1;
        }
//        data_list=new ArrayList<>();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home_Get_Set item = new Home_Get_Set();
                    item.fb_id = itemdata.optString("fb_id");


                    JSONObject user_info = itemdata.optJSONObject("user_info");

                    item.username = user_info.optString("username");
                    item.first_name = user_info.optString("first_name", context.getResources().getString(R.string.app_name));
                    item.last_name = user_info.optString("last_name", "User");
                    item.profile_pic = user_info.optString("profile_pic", "null");
                    item.verified = user_info.optString("verified");

                    JSONObject sound_data = itemdata.optJSONObject("sound");
                    item.sound_id = sound_data.optString("id");
                    item.sound_name = sound_data.optString("sound_name");
                    item.sound_pic = sound_data.optString("thum");


                    JSONObject count = itemdata.optJSONObject("count");
                    item.like_count = count.optString("like_count");
                    item.video_comment_count = count.optString("video_comment_count");


                    item.video_id = itemdata.optString("id");
                    item.liked = itemdata.optString("liked");
                    item.video_url = itemdata.optString("video");
                    item.video_description = itemdata.optString("description");

                    item.thum = itemdata.optString("thum");
                    item.created_date = itemdata.optString("created");

                    if (!map.containsKey(itemdata.optString("id"))) {
                        map.put(itemdata.optString("id"), "");
                        data_list.add(item);
                    }
                }

                if(isAdapter == 0)
                Set_Adapter();
                else
                {

                    adapter.notifyDataSetChanged();

                }


            }else {
                Toast.makeText(context, "Nothing more to show", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private void Call_Api_For_Singlevideos(final int postion) {


        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("token",Variables.sharedPreferences.getString(Variables.device_token,"Null"));
            parameters.put("video_id",data_list.get(postion).video_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


       /* ApiRequest.Call_Api(context, Variables.showAllVideos, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                //regular
             //   swiperefresh.setRefreshing(false);
            //    Singal_Video_Parse_data(postion,resp);
            }
        });*/


    }

    public void Singal_Video_Parse_data(int pos,String responce){

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    Home_Get_Set item=new Home_Get_Set();
                    item.fb_id=itemdata.optString("fb_id");

                    JSONObject user_info=itemdata.optJSONObject("user_info");

                    item.username=user_info.optString("username");
                    item.first_name=user_info.optString("first_name",context.getResources().getString(R.string.app_name));
                    item.last_name=user_info.optString("last_name","User");
                    item.profile_pic=user_info.optString("profile_pic","null");
                    item.verified=user_info.optString("verified");

                    JSONObject sound_data=itemdata.optJSONObject("sound");
                    item.sound_id=sound_data.optString("id");
                    item.sound_name=sound_data.optString("sound_name");
                    item.sound_pic=sound_data.optString("thum");



                    JSONObject count=itemdata.optJSONObject("count");
                    item.like_count=count.optString("like_count");
                    item.video_comment_count=count.optString("video_comment_count");


                    item.video_id=itemdata.optString("id");
                    item.liked=itemdata.optString("liked");
                    item.video_url=itemdata.optString("video");
                    item.video_description=itemdata.optString("description");

                    item.thum=itemdata.optString("thum");
                    item.created_date=itemdata.optString("created");

                    data_list.remove(pos);
                    data_list.add(pos,item);
                    adapter.notifyDataSetChanged();
                }



            }else {
              //  Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }



     public void Set_Player (final int currentPage) {

            final Home_Get_Set item= data_list.get(currentPage);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));


          View layout=layoutManager.findViewByPosition(currentPage);




        final SimpleExoPlayer player;
        HttpProxyCacheServer proxy = TicTic.getProxy(getActivity());
        final PlayerView playerView=layout.findViewById(R.id.playerview);
        proxy.registerCacheListener(this, item.video_url);

        boolean fullyCached = proxy.isCached(item.video_url);
        if (fullyCached) {
            //p_bar.setVisibility(View.GONE);
            String proxyUrl = proxy.getProxyUrl(item.video_url);

            SimpleExoPlayer player2 = newSimpleExoPlayer();



            playerView.setPlayer(player2);
            MediaSource videoSource = newVideoSource(proxyUrl);
            player2.prepare(videoSource);
            player2.setRepeatMode(Player.REPEAT_MODE_ALL);
            player2.setPlayWhenReady(is_visible_to_user);
            player2.addListener(this);

            player = player2;
            privious_player=player;

        }else{
            String proxyUrl = proxy.getProxyUrl(item.video_url);

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            SimpleExoPlayer player2 = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(proxyUrl));
            player2.prepare(videoSource);
            player2.setRepeatMode(Player.REPEAT_MODE_ALL);
            player2.addListener(this);
            playerView.setPlayer(player2);
            player2.setPlayWhenReady(is_visible_to_user);
            privious_player=player2;
            player = player2;


        }

        if(currentPage+1!=data_list.size())
            nextURL = data_list.get(currentPage+1).video_url;

        if(data_list.size()>5)
        {
            if(currentPage+1 == data_list.size() - 5)
            {
                Call_Api_For_get_Allvideos();
            }
        }


        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                     super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if(deltaX > 0)
                        {
                            OpenProfile(item,true);
                        }
                    }


                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if(!player.getPlayWhenReady()){
                        is_user_stop_video=false;
                        privious_player.setPlayWhenReady(true);
                    }else{
                        is_user_stop_video=true;
                        privious_player.setPlayWhenReady(false);
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    if(!player.getPlayWhenReady()){
                        is_user_stop_video=false;
                        privious_player.setPlayWhenReady(true);
                    }


                    if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                        Show_heart_on_DoubleTap(item, mainlayout, e);
                        Like_Video(currentPage, item);
                    }else {
                        Toast.makeText(context, "Please Login into app", Toast.LENGTH_SHORT).show();
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        TextView desc_txt=layout.findViewById(R.id.desc_txt);
        HashTagHelper.Creator.create(context.getResources().getColor(R.color.maincolor), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {

                onPause();
                OpenHashtag(hashTag);

            }
        }).handle(desc_txt);



        /*LinearLayout soundimage = (LinearLayout)layout.findViewById(R.id.sound_image_layout);
        Animation sound_animation = AnimationUtils.loadAnimation(context,R.anim.d_clockwise_rotation);
        soundimage.startAnimation(sound_animation);*/

        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false))
            if(!Variables.sharedPreferences.getString(Variables.u_id,"0").equals(item.fb_id) )

                Functions.Call_Api_For_update_view(getActivity(),item.video_id);


        swipe_count++;
        if(swipe_count>4){
            Show_add();
            swipe_count=0;
        }



        Call_Api_For_Singlevideos(currentPage);

    }


    private SimpleExoPlayer newSimpleExoPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        return ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
    }

    private MediaSource newVideoSource(String url) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(getActivity(), "AndroidVideoCache sample");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
    }


    public void Show_heart_on_DoubleTap(Home_Get_Set item,final RelativeLayout mainlayout,MotionEvent e){

        int x = (int) e.getX()-100;
        int y = (int) e.getY()-100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if(item.liked.equals("1"))
        iv.setImageDrawable(getResources().getDrawable(
                R.drawable.ic_like2));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.sasa));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context,R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv.startAnimation(fadeoutani);

    }



    public void Show_add(){
        if(mInterstitialAd!=null && mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }


    @Override
    public void onDataSent(String yourData) {
        int comment_count =Integer.parseInt(yourData);
        Home_Get_Set item=data_list.get(currentPage);
        item.video_comment_count=""+comment_count;
        data_list.remove(currentPage);
        data_list.add(currentPage,item);
        adapter.notifyDataSetChanged();
    }



    // this will call when go to the home tab From other tab.
    // this is very importent when for video play and pause when the focus is changes
    boolean is_visible_to_user;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        is_visible_to_user=isVisibleToUser;

        if(privious_player!=null && (isVisibleToUser && !is_user_stop_video)){
            privious_player.setPlayWhenReady(true);
        }else if(privious_player!=null && !isVisibleToUser){
            privious_player.setPlayWhenReady(false);
        }
    }



   // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;
    public void Release_Privious_Player(){
        if(privious_player!=null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }




    // this function will call for like the video and Call an Api for like the video
    public void Like_Video(final int position, final Home_Get_Set home_get_set){
        String action=home_get_set.liked;

        if(action.equals("1")){
            action="0";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) -1);
        }else {

            action="1";
            home_get_set.like_count=""+(Integer.parseInt(home_get_set.like_count) +1);
        }


        data_list.remove(position);
        home_get_set.liked=action;
        data_list.add(position,home_get_set);
        adapter.notifyDataSetChanged();

        Functions.Call_Api_For_like_video(getActivity(), home_get_set.video_id, action,new API_CallBack() {

            @Override
            public void ArrayData(ArrayList arrayList) {

            }

            @Override
            public void OnSuccess(String responce) {

                int s = 0;
            }

            @Override
            public void OnFail(String responce) {

            }
        });

    }



    // this will open the comment screen
    private void OpenComment(Home_Get_Set item) {

        int comment_counnt=Integer.parseInt(item.video_comment_count);

        Fragment_Data_Send fragment_data_send=this;

        Comment_F comment_f = new Comment_F(comment_counnt,fragment_data_send);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id",item.video_id);
        args.putString("user_id",item.fb_id);
        comment_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, comment_f).commit();


    }



    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenProfile(Home_Get_Set item,boolean from_right_to_left) {
        if(Variables.sharedPreferences.getString(Variables.u_id,"0").equals(item.fb_id)){

            TabLayout.Tab profile= MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        }else {
            Profile_F profile_f = new Profile_F(new Fragment_Callback() {
                @Override
                public void Responce(Bundle bundle) {
                    Call_Api_For_Singlevideos(currentPage);
                }
            });
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if(from_right_to_left)
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.fb_id);
            args.putString("user_name",item.first_name+" "+item.last_name);
            args.putString("user_pic",item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

            Taged_Videos_F taged_videos_f = new Taged_Videos_F();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
            Bundle args = new Bundle();
            args.putString("tag", tag);
            taged_videos_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, taged_videos_f).commit();


    }



    private void Show_video_option(final Home_Get_Set home_get_set) {

        final CharSequence[] options = { "Save Video","Report","I don't like it", "Cancel" };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Save Video"))

                {
                    if(Functions.Checkstoragepermision(getActivity()))
                    Save_Video(home_get_set);

                }




                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }  else
                if (options[item].equals("Report"))

                {
                    dialog.dismiss();
                    ShowReport(home_get_set);

                }else if (options[item].equals("I don't like it")){

                    String action = options[item].toString();

                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);

                    SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(context) ;

                    String getDate = s.getString("date","");
                    int count = s.getInt("unlike",0);

                    if(getDate.equals(formattedDate)  )
                    {
                        if(count>7)
                        {

                            Toast.makeText(context,"Try again after sometime", Toast.LENGTH_SHORT).show();
                        }else{

                            SharedPreferences.Editor e = s.edit();

                            report(action,home_get_set);
                            e.putString("date", formattedDate).apply();
                            count++;
                            e.putInt("unlike",count);
                            e.apply();
                        }
                    }else{

                        report(action,home_get_set);
                        SharedPreferences.Editor e = s.edit();

                        e.putString("date", formattedDate).apply();
                        count=0;
                        e.putInt("unlike",count);
                        e.apply();
                    }




                }

            }

        });

        builder.show();

    }
    void report( String a, Home_Get_Set home_get_set){

        if(adapter.getItemCount()>currentPage+1)
        recyclerView.smoothScrollToPosition(currentPage+1);

        String action = a;
        Functions.Call_Api_For_report_video(getActivity(), home_get_set.video_id, action,new API_CallBack() {

            @Override
            public void ArrayData(ArrayList arrayList) {

            }

            @Override
            public void OnSuccess(String responce) {

                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {


                        //Toast. makeText(getApplicationContext(),"Video disliked",Toast. LENGTH_SHORT).show();



                    }
                });

            }

            @Override
            public void OnFail(String responce) {

            }
        });
    }

    private void ShowReport(final Home_Get_Set home_get_set) {

        final CharSequence[] options = { "Nudity or sexual activity",
                "Hate speech or symbols",
                "Violence or dangerous organizations",
                "Sale of illegal or regulated goods",
                "Bullying or harassment",
                "Intellectual property violation",
                "Suicide, self injury or eating disorders",
                "Scam or fraud",
                "False information",
                "Copyright Issues",
                "Cancel"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }else{
                    String action = options[item].toString();

                    Date c = Calendar.getInstance().getTime();
                    System.out.println("Current time => " + c);

                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c);

                    SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(context) ;

                    String getDate = s.getString("date","");
                    int count = s.getInt("report",0);

                    if(getDate.equals(formattedDate)  )
                    {
                        if(count>7)
                        {

                            Toast.makeText(context,"Try again after sometime", Toast.LENGTH_SHORT).show();
                            return;
                        }else{

                            SharedPreferences.Editor e = s.edit();

                             e.putString("date", formattedDate).apply();
                            count++;
                            e.putInt("report",count);
                            e.apply();
                        }
                    }else{

                         SharedPreferences.Editor e = s.edit();

                        e.putString("date", formattedDate).apply();
                        count=0;
                        e.putInt("report",count);
                        e.apply();
                    }
                    if(adapter.getItemCount()>currentPage+1)
                        recyclerView.smoothScrollToPosition(currentPage+1);

                     Functions.Call_Api_For_report_video(getActivity(), home_get_set.video_id, action,new API_CallBack() {

                        @Override
                        public void ArrayData(ArrayList arrayList) {

                        }

                        @Override
                        public void OnSuccess(String responce) {

                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                   Toast. makeText(getApplicationContext(),"Video reported",Toast. LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void OnFail(String responce) {

                        }
                    });

                }

            }

        });

        builder.show();

    }





    public void Save_Video(final Home_Get_Set item){

        Functions.Show_determinent_loader(context,false,false);
        PRDownloader.initialize(getActivity().getApplicationContext());
        DownloadRequest prDownloader= PRDownloader.download(item.video_url, Variables.app_folder, item.video_id+"no_watermark"+".mp4")
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        int prog=(int)((progress.currentBytes*100)/progress.totalBytes);
                        Functions.Show_loading_progress(prog/2);

                    }
                });


              prDownloader.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    Applywatermark(item);
                   }

                @Override
                public void onError(Error error) {
                    Delete_file_no_watermark(item);
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    Functions.cancel_determinent_loader();
                }


            });



    }

    public void Applywatermark(final Home_Get_Set item){

         Bitmap myLogo = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_watermark_image)).getBitmap();
         Bitmap bitmap_resize=Bitmap.createScaledBitmap(myLogo, 50, 30, false);
         GlWatermarkFilter filter=new GlWatermarkFilter(bitmap_resize, GlWatermarkFilter.Position.LEFT_TOP);
         new GPUMp4Composer(Variables.app_folder+item.video_id+"no_watermark"+".mp4",
                Variables.app_folder+item.video_id+".mp4")
                .filter(filter)

                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)((progress*100)/2)+50);

                    }

                    @Override
                    public void onCompleted() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();
                                Delete_file_no_watermark(item);
                                Scan_file(item);

                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp",exception.toString());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Delete_file_no_watermark(item);
                                    Functions.cancel_determinent_loader();
                                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();

                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();
    }


    public void Delete_file_no_watermark(Home_Get_Set item){
        File file=new File(Variables.app_folder+item.video_id+"no_watermark"+".mp4");
        if(file.exists()){
            file.delete();
        }
    }

    public void Scan_file(Home_Get_Set item){
        MediaScannerConnection.scanFile(getActivity(),
                new String[] { Variables.app_folder+item.video_id+".mp4" },
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }



    public boolean is_fragment_exits(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if(fm.getBackStackEntryCount()==0){
            return false;
        }else {
            return true;
        }

    }

    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onResume() {
        super.onResume();

        if((privious_player!=null && (is_visible_to_user && !is_user_stop_video)) && !is_fragment_exits() ){
            privious_player.setPlayWhenReady(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }

    }


    @Override
    public void onStop() {
        super.onStop();

        if(privious_player!=null){
            privious_player.setPlayWhenReady(false);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(privious_player!=null){
            privious_player.release();
        }
    }



    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
        }else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }




    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }


    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }


    @Override
    public void onLoadingChanged(boolean isLoading) {

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(playbackState==Player.STATE_BUFFERING){
            p_bar.setVisibility(View.VISIBLE);
        }
        else if(playbackState==Player.STATE_READY){
             p_bar.setVisibility(View.GONE);
             try {
            //  URL  url = new URL(cachingUrl(nextURL));

                 if(nextURL!=null) {
                     HttpProxyCacheServer proxy = TicTic.getProxy(getActivity());

                     boolean fullyCached = proxy.isCached(nextURL);
                     if(!fullyCached)
                         new SaveVideoBg(getActivity()).execute(nextURL);
                 }



             } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

    String nextURL;


    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    @Override
    public void onSeekProcessed() {

    }



}
