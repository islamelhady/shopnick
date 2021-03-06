package com.theleafapps.pro.shopnick.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.theleafapps.pro.shopnick.R;
import com.theleafapps.pro.shopnick.adapters.ExpandableListAdapter;
import com.theleafapps.pro.shopnick.adapters.ViewPagerAdapter;
import com.theleafapps.pro.shopnick.dialogs.MyProgressDialog;
import com.theleafapps.pro.shopnick.models.Category;
import com.theleafapps.pro.shopnick.models.Customer;
import com.theleafapps.pro.shopnick.models.ExpandedMenuModel;
import com.theleafapps.pro.shopnick.models.SubCategory;
import com.theleafapps.pro.shopnick.models.multiples.CartItems;
import com.theleafapps.pro.shopnick.models.multiples.Categories;
import com.theleafapps.pro.shopnick.models.multiples.SubCategories;
import com.theleafapps.pro.shopnick.tasks.GetAllCartItemTask;
import com.theleafapps.pro.shopnick.tasks.GetAllCategoriesTask;
import com.theleafapps.pro.shopnick.tasks.GetAllSubCategoriesTask;
import com.theleafapps.pro.shopnick.tasks.GetCustomerByIdTask;
import com.theleafapps.pro.shopnick.ui.fragments.OneFragment;
import com.theleafapps.pro.shopnick.utils.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShowcaseActivity extends AppCompatActivity {

    static String NAME = "Leaf Apps";
    static String EMAIL = "theleafapps@gmail.com";
    TextView customer_name, customer_email;
    List<Category> categoriesRec;
    List<SubCategory> subCategoriesRec;
    List<ExpandedMenuModel> listDataHeader;
    NavigationView navigationView;
    DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    MyProgressDialog myProgressDialog;
    MenuItem menuItem;
    ImageButton nav_about_us, nav_credits;
    Menu menu;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;                                     // Declaring the Toolbar Object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_showcase);

            myProgressDialog = new MyProgressDialog(this);
            toolbar = (Toolbar) findViewById(R.id.toolbar_showcase);
            setSupportActionBar(toolbar);

            getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_drawer);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setIcon(R.drawable.logo_small);

            if (!Commons.hasActiveInternetConnection(this)) {
                Intent intent1 = new Intent(this, NoNetworkActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }

            if (menu != null) {
                menuItem = menu.findItem(R.id.cart_icon);
                if (Commons.cartItemCount < 1) {
                    menuItem.setIcon(R.drawable.cart);
                } else {
                    menuItem.setIcon(R.drawable.cartfull);
                }
            }

            //Commons.getActivityTrail(this);

            nav_about_us = (ImageButton) findViewById(R.id.nav_about_us);
            nav_credits = (ImageButton) findViewById(R.id.nav_credits);
            customer_name = (TextView) findViewById(R.id.customer_name);
            customer_email = (TextView) findViewById(R.id.customer_email);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout_showcase);
            expandableList = (ExpandableListView) findViewById(R.id.exp_navigation_menu);
            navigationView = (NavigationView) findViewById(R.id.nav_view);

            nav_about_us.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(ShowcaseActivity.this,"About us clicked",Toast.LENGTH_LONG).show();
                    Intent intent_about = new Intent(ShowcaseActivity.this, AboutUsActivity.class);
                    startActivity(intent_about);
                }
            });

            nav_credits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent_about = new Intent(ShowcaseActivity.this, CreditsActivity.class);
                    startActivity(intent_about);
                }
            });

            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }

            expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                    //Log.d("DEBUG", "submenu item clicked");
                    return false;
                }
            });

            expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                    //Log.d("DEBUG", "heading clicked");
                    return false;
                }
            });

            SharedPreferences sharedPreferences = getSharedPreferences("Shopnick", Context.MODE_PRIVATE);
            String cid = sharedPreferences.getString("cid", "");

            if (!TextUtils.isEmpty(cid) && Integer.parseInt(cid) != 0) {
                GetCustomerByIdTask getCustomerByIdTask = new GetCustomerByIdTask(this, Integer.parseInt(cid));
                getCustomerByIdTask.execute().get();

                Customer customer = getCustomerByIdTask.customerRec;
                NAME = customer.first_name + " " + customer.last_name;
                EMAIL = customer.email;
            }
            customer_name.setText(NAME);
            customer_email.setText(EMAIL);

            if (Commons.hasActiveInternetConnection(this)) {
                Log.d("Tangho", "Network Connected");

                Intent intent = getIntent();
                int catId = intent.getIntExtra("categoryId", 0);

                if (!TextUtils.isEmpty(cid) && Integer.parseInt(cid) != 0) {
                    GetAllCartItemTask getAllCartItemTask = new GetAllCartItemTask(this, Integer.parseInt(cid));
                    getAllCartItemTask.execute().get();


                    CartItems cartItems = getAllCartItemTask.cartItemsReceived;
                    if (cartItems != null && cartItems.cartItemList.size() > 0) {
                        Commons.cartItemCount = cartItems.cartItemList.size();
                    }
                }

                /*****
                 * Getting All Categories
                 ******/
                GetAllCategoriesTask getAllCategoriesTask
                        = new GetAllCategoriesTask(this);
                boolean x = getAllCategoriesTask.execute().get();
                Categories cat = getAllCategoriesTask.categoriesReceived;

                if (cat != null && cat.categories.size() > 0) {
                    //Toast.makeText(this, cat.categories.size() + " Categories have been received", Toast.LENGTH_SHORT).show();
                    categoriesRec = cat.categories;

                    getAllCategoriesTask = null;
                }


                /*****
                 * Getting All Sub Categories
                 ******/

                if (Commons.catIdToSubCatMap.size() == 0) {
                    GetAllSubCategoriesTask getAllSubCategoriesTask = new GetAllSubCategoriesTask(this);
                    boolean y = getAllSubCategoriesTask.execute().get();

                    SubCategories subCat = getAllSubCategoriesTask.subCategoriesReceived;

                    if (cat != null && cat.categories.size() > 0) {
                        //                Toast.makeText(this, cat.categories.size() + " Categories have been received", Toast.LENGTH_SHORT).show();
                        subCategoriesRec = subCat.subCategories;

                        /*****
                         * Storing All SubCategories corresponding to CategoryId in Common Hashmap
                         ******/

                        for (SubCategory sc : subCategoriesRec) {

                            if (TextUtils.isEmpty(sc.image_url)) {
                                sc.image_url = "http://dummyimage.com/180x100/000/fff&text=" + sc.sub_category_name.replace(" ", "");
                            }

                            if (Commons.catIdToSubCatMap.containsKey(sc.category_id)) {
                                Commons.catIdToSubCatMap.get(sc.category_id).add(sc);
                            } else {
                                List<SubCategory> list = new ArrayList<>();
                                list.add(sc);
                                Commons.catIdToSubCatMap.put(sc.category_id, list);
                            }
                        }
                    }
                }

                prepareListData();

                mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

                // setting list adapter
                expandableList.setAdapter(mMenuAdapter);

                viewPager = (ViewPager) findViewById(R.id.viewpager);
                setupViewPager(viewPager);

                tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);

                if (catId > 0)
                    viewPager.setCurrentItem(catId);

            } else {
                Log.d("Tangho", "Network Disconnected");
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if (categoriesRec != null && categoriesRec.size() > 0) {

            for (int i = 0; i < categoriesRec.size(); i++) {

                int category_id = categoriesRec.get(i).category_id;
                String name = categoriesRec.get(i).category_name;
                Fragment abc = new OneFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("progressDialog", myProgressDialog);
                bundle.putInt("category_id", category_id);
                abc.setArguments(bundle);
                adapter.addFrag(abc, name);
            }
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_product_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_icon);
        this.menu = menu;
        if (Commons.cartItemCount > 0)
            menuItem.setIcon(R.drawable.cartfull);
        else
            menuItem.setIcon(R.drawable.cart);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.user_profile:
                intent = new Intent(this, CustomerProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.cart_icon:
//                Toast.makeText(this,"Cart Menu Clicked",Toast.LENGTH_LONG).show();
                intent = new Intent(this, CartActivity.class);
                intent.putExtra("caller", "ShowcaseActivity");
                startActivity(intent);
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void prepareListData() {
        int i = 0;
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        if (categoriesRec != null && categoriesRec.size() > 0) {
            for (Category category : categoriesRec) {

                ExpandedMenuModel item = new ExpandedMenuModel();
                item.setIconName(category.category_name);

                // Adding data header
                listDataHeader.add(item);

                List<String> heading = new ArrayList<String>();
                List<SubCategory> subCats = Commons.catIdToSubCatMap.get(category.category_id);

                for (SubCategory subcat : subCats) {
                    heading.add(subcat.sub_category_name);
                }
                listDataChild.put(listDataHeader.get(i), heading);
                i++;
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Tangho", "Showcase activity >> onRestart Called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myProgressDialog.isShowing())
            finish();
        Log.d("Tangho", "Showcase activity >> onPause Called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myProgressDialog.dismiss();
        Log.d("Tangho", "Showcase activity >> onDestroy Called");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("Tangho", "Showcase activity >> onBackPressed Called");
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d("Tangho", "Showcase activity >> OnResume Called");

        if (menu != null) {
            menuItem = menu.findItem(R.id.cart_icon);
            if (Commons.cartItemCount < 1) {
                menuItem.setIcon(R.drawable.cart);
            } else {
                menuItem.setIcon(R.drawable.cartfull);
            }
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
}

