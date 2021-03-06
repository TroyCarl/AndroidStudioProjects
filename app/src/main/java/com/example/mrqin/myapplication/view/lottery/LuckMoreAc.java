package com.example.mrqin.myapplication.view.lottery;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mrqin.myapplication.R;
import com.example.mrqin.myapplication.model.LuckAdapterBean;
import com.example.mrqin.myapplication.model.LuckBean;
import com.example.mrqin.myapplication.utils.APPID;
import com.example.mrqin.myapplication.utils.NetworkUtil;
import com.example.mrqin.myapplication.utils.TextViewUtils;
import com.example.mrqin.myapplication.view.BaseActivity;
import com.example.mrqin.myapplication.view.custom.RatingBar;
import com.google.gson.Gson;
import com.show.api.ShowApiRequest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 运势详情页面
 */
public class LuckMoreAc extends BaseActivity {
    private MyHandler mHandler;
    private ImageView ll_TitleBar_back;

    TabLayout tabLayout;
    ViewPager vp;
    private String mTitle[] = new String[]{
            "今日",
            "明日",
            "本周",
            "本月",
            "今年"
    };
    private Resources res;
    private int[] item_constellation_img;
    private String[] item_more_constellation_txt;

    private static List<LuckAdapterBean> mData = new ArrayList<>();

    private RelativeLayout noNetLayout;
    private Button refreshBtn;

    private static LuckBean luckBean;

    private String lotteryName = "ssq";
    private int starPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.luck_more_layout);
        lotteryName = getIntent().getStringExtra("name");
        if (lotteryName == null) {
            lotteryName = "ssq";
        }
        starPosition = getIntent().getIntExtra("position", 0);
        init();
        initData();
    }

    private void initData() {
        res = getResources();
        item_constellation_img = new int[]{R.mipmap.love_ic_aries,
                R.mipmap.love_ic_taurus, R.mipmap.love_ic_gemin,
                R.mipmap.love_ic_cancer, R.mipmap.love_ic_leo,
                R.mipmap.love_ic_virgo, R.mipmap.love_ic_libra,
                R.mipmap.love_ic_scorpio, R.mipmap.love_ic_sagittarius,
                R.mipmap.love_ic_capricornus, R.mipmap.love_ic_aquarius,
                R.mipmap.love_ic_pisces};
        item_more_constellation_txt = res
                .getStringArray(R.array.ac_marry_constellation);
    }

    private void init() {
        mHandler = new MyHandler(this);
        ll_TitleBar_back = (ImageView) findViewById(R.id.ll_TitleBar_back);
        ll_TitleBar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LuckMoreAc.this.finish();
            }
        });

        noNetLayout = findViewById(R.id.noNetLayout);
        refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.isNetworkConnected(LuckMoreAc.this)) {
                    noNetLayout.setVisibility(View.GONE);
                    startGetData();
                } else {
                    Snackbar.make(view, R.string.lotter_no_net, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        tabLayout = findViewById(R.id.tabLayout);
        vp = findViewById(R.id.vp_content);

        startGetData();
    }

    private void startGetData() {
        if (NetworkUtil.getNetworkType(LuckMoreAc.this) == 0) {
            noNetLayout.setVisibility(View.VISIBLE);
        } else {
            startThread();
        }
    }

    private void startThread() {
        new Thread() {
            //在新线程中发送网络请求
            public void run() {
                String appid = APPID.APP_ID;//要替换成自己的
                String secret = APPID.APP_SCREAT;//要替换成自己的
                final String res = new ShowApiRequest("http://route.showapi.com/872-1", appid, secret)
                        .addTextPara("star", TextViewUtils.getStar(starPosition))
                        .addTextPara("needTomorrow", "1")
                        .addTextPara("needWeek", "1")
                        .addTextPara("needMonth", "1")
                        .addTextPara("needYear", "1")
                        .post();
                System.out.println(res);
                mHandler.obtainMessage(APPID.MSG_DATA_SUCCESS_LOTTERY_FRG, res).sendToTarget();
            }
        }.start();
    }

    private void setData() {
        MyAdapter adapter = new MyAdapter();
        vp.setAdapter(adapter);
        tabLayout.setupWithViewPager(vp);
    }

    class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mTitle.length;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(LuckMoreAc.this).inflate(R.layout.luck_item_view_layout, null);
            TextView day_notice = view.findViewById(R.id.day_notice);
            TextView general_txt = view.findViewById(R.id.general_txt);
            TextView love_txt = view.findViewById(R.id.love_txt);
            TextView lucky_direction = view.findViewById(R.id.lucky_direction);
            TextView money_txt = view.findViewById(R.id.money_txt);
            TextView work_txt = view.findViewById(R.id.work_txt);
            RatingBar love_star = view.findViewById(R.id.love_star);
            RatingBar money_star = view.findViewById(R.id.money_star);
            RatingBar summary_star = view.findViewById(R.id.summary_star);
            RatingBar work_star = view.findViewById(R.id.work_star);
            TextView lucky_num = view.findViewById(R.id.lucky_num);
            ImageView luck_star_img = view.findViewById(R.id.luck_star_img);
            TextView luck_star_name = view.findViewById(R.id.luck_star_name);
            LuckAdapterBean bean = mData.get(position);

            day_notice.setText(getStrIsNull(bean.getDay_notice()));
            general_txt.setText(getStrIsNull(bean.getGeneral_txt()));
            money_txt.setText(getStrIsNull(bean.getMoney_txt()));
            love_txt.setText(getStrIsNull(bean.getLove_txt()));
            lucky_direction.setText(getStrIsNull(bean.getLucky_direction()));
            work_txt.setText(getStrIsNull(bean.getWork_txt()));
            lucky_num.setText(getStrIsNull(bean.getLucky_num()));
            love_star.setRating(bean.getLove_star());
            money_star.setRating(bean.getMoney_star());
            summary_star.setRating(bean.getSummary_star());
            work_star.setRating(bean.getWork_star());
            luck_star_img.setImageResource(item_constellation_img[starPosition]);
            luck_star_name.setText(item_more_constellation_txt[starPosition]);
            container.addView(view);
            return view;
        }

        public String getStrIsNull(String s) {
            String str = "暂无";
            if (s == null) {
                s = str;
            }
            return s;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitle[position];
        }
    }


    static class MyHandler extends Handler {

        private WeakReference<LuckMoreAc> mOuter;

        private MyHandler(LuckMoreAc outer) {
            mOuter = new WeakReference<LuckMoreAc>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            LuckMoreAc outer = mOuter.get();
            if (outer != null) {
                //操作
                switch (msg.what) {
                    case APPID.MSG_DATA_SUCCESS_LOTTERY_FRG:
                        Gson gson = new Gson();
                        luckBean = gson.fromJson((String) msg.obj, LuckBean.class);
                        mData.clear();
                        mData.add(getBean(luckBean, 0));
                        mData.add(getBean(luckBean, 1));
                        mData.add(getBean(luckBean, 2));
                        mData.add(getBean(luckBean, 3));
                        mData.add(getBean(luckBean, 4));
                        outer.setData();
                        break;
                }
            }
        }
    }

    private static LuckAdapterBean getBean(LuckBean luckBean, int which) {
        LuckBean.ShowapiResBodyBean resBody = luckBean.getShowapi_res_body();
        String day_notice = null;
        String general_txt = null;
        String money_txt = null;
        String love_txt = null;
        String lucky_direction = null;
        String work_txt = null;
        String lucky_num = null;
        int love_star = 0;
        int money_star = 0;
        int summary_star = 0;
        int work_star = 0;
        switch (which) {
            case 0:
                day_notice = resBody.getDay().getDay_notice();
                general_txt = resBody.getDay().getGeneral_txt();
                money_txt = resBody.getDay().getMoney_txt();
                love_txt = resBody.getDay().getLove_txt();
                lucky_direction = resBody.getDay().getLucky_direction();
                work_txt = resBody.getDay().getWork_txt();
                lucky_num = resBody.getDay().getLucky_num();
                love_star = resBody.getDay().getLove_star();
                money_star = resBody.getDay().getMoney_star();
                summary_star = resBody.getDay().getSummary_star();
                work_star = resBody.getDay().getWork_star();
                break;
            case 1:
                day_notice = resBody.getTomorrow().getDay_notice();
                general_txt = resBody.getTomorrow().getGeneral_txt();
                money_txt = resBody.getTomorrow().getMoney_txt();
                love_txt = resBody.getTomorrow().getLove_txt();
                lucky_direction = resBody.getTomorrow().getLucky_direction();
                work_txt = resBody.getTomorrow().getWork_txt();
                lucky_num = resBody.getTomorrow().getLucky_num();
                love_star = resBody.getTomorrow().getLove_star();
                money_star = resBody.getTomorrow().getMoney_star();
                summary_star = resBody.getTomorrow().getSummary_star();
                work_star = resBody.getTomorrow().getWork_star();
                break;
            case 2:
                day_notice = resBody.getWeek().getDay_notice();
                general_txt = resBody.getWeek().getGeneral_txt();
                money_txt = resBody.getWeek().getMoney_txt();
                love_txt = resBody.getWeek().getLove_txt();
                lucky_direction = resBody.getWeek().getLucky_direction();
                work_txt = resBody.getWeek().getWork_txt();
                lucky_num = resBody.getWeek().getLucky_num();
                love_star = resBody.getWeek().getLove_star();
                money_star = resBody.getWeek().getMoney_star();
                summary_star = resBody.getWeek().getSummary_star();
                work_star = resBody.getWeek().getWork_star();
                break;
            case 3:
                day_notice = resBody.getMonth().getDay_notice();
                general_txt = resBody.getMonth().getGeneral_txt();
                money_txt = resBody.getMonth().getMoney_txt();
                love_txt = resBody.getMonth().getLove_txt();
                lucky_direction = resBody.getMonth().getLucky_direction();
                work_txt = resBody.getMonth().getWork_txt();
                lucky_num = resBody.getMonth().getLucky_num();
                love_star = resBody.getMonth().getLove_star();
                money_star = resBody.getMonth().getMoney_star();
                summary_star = resBody.getMonth().getSummary_star();
                work_star = resBody.getMonth().getWork_star();
                break;
            case 4:
                day_notice = resBody.getYear().getDay_notice();
                general_txt = resBody.getYear().getGeneral_txt();
                money_txt = resBody.getYear().getMoney_txt();
                love_txt = resBody.getYear().getLove_txt();
                lucky_direction = resBody.getYear().getLucky_direction();
                work_txt = resBody.getYear().getWork_txt();
                lucky_num = resBody.getYear().getLucky_num();
                love_star = resBody.getYear().getLove_star();
                money_star = resBody.getYear().getMoney_star();
                summary_star = resBody.getYear().getSummary_star();
                work_star = resBody.getYear().getWork_star();
                break;
        }
        LuckAdapterBean bean = new LuckAdapterBean(day_notice, general_txt, null, love_star, love_txt, lucky_direction, lucky_num, null, money_star, money_txt, summary_star, null, work_star, work_txt);
        return bean;
    }
}
