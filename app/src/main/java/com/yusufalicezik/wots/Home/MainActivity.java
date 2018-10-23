package com.yusufalicezik.wots.Home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.RelativeDateTimeFormatter;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseArray;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yusufalicezik.wots.Login.AcilisActivity;

import com.yusufalicezik.wots.Model.ArkadasPost;
import com.yusufalicezik.wots.Model.Posts;
import com.yusufalicezik.wots.R;

import com.yusufalicezik.wots.Utils.BottomNavigationViewHelper;
import com.yusufalicezik.wots.Utils.GridImageView;
import com.yusufalicezik.wots.Utils.TimeAgo;
import com.yusufalicezik.wots.Utils.UniversalImagLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private RecyclerView postListe;
    private DatabaseReference mRef,mRef2, likesRef;

    private SwipeRefreshLayout swipeRefreshLayout;




    private int ACTIVITY_NO=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mRef= FirebaseDatabase.getInstance().getReference().child("posts");
        likesRef= FirebaseDatabase.getInstance().getReference().child("likes");
        mRef2= FirebaseDatabase.getInstance().getReference().child("arkadas").child(mAuth.getCurrentUser().getUid());
        swipeRefreshLayout=findViewById(R.id.swiperefresh);
        postListe=findViewById(R.id.anasayfaRecyclerListe);


        postListe.setHasFixedSize(true);
       // postListe.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager ll=new LinearLayoutManager(this);
        ll.setReverseLayout(true);
        ll.setStackFromEnd(true);
        postListe.setLayoutManager(ll);



        setupAuthListener();
        BottomMenuyuAyarla();
        UniversalInit();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                gonderilerinTumunuGetir();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    private void setupAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent=new Intent(getApplicationContext(),AcilisActivity.class);
                    startActivity(intent);
                    finish();

                }
                else {

                }

            }
        };
    }
    private void BottomMenuyuAyarla() {


        BottomNavigationViewEx bottomNavigationViewEx=findViewById(R.id.bottomNavigationView);

        BottomNavigationViewHelper bottomNavigationViewHepler=new BottomNavigationViewHelper(bottomNavigationViewEx);
        bottomNavigationViewHepler.setupNavigation(getApplicationContext(),bottomNavigationViewEx);

        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NO);
        menuItem.setChecked(true);


    }

    @Override
    protected void onResume() {
        BottomMenuyuAyarla();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //burda alert dialog ile sorulur, çıkmak istiyor musunuz? belki..
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener (mAuthListener);

        gonderilerinTumunuGetir();
    }

    private void gonderilerinTumunuGetir() {
      //  Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();
     //   Query searchPeopleandFriendsQuery = mRef.orderByChild("yuklenme_tarih");
     //   Query searchPeopleandFriendsQuery = mRef.orderByChild("yuklenme_tarih");




        final ArrayList<Posts> pDizi=new ArrayList<>();

        mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null)
                {
                    for(DataSnapshot dd:dataSnapshot.getChildren())
                    {
                        final ArkadasPost l=dd.getValue(ArkadasPost.class);

                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null) {
                                    for (DataSnapshot dd2 : dataSnapshot.getChildren()) {

                                        Posts l2=dd2.getValue(Posts.class);
                                        if(l.getUser_arkadas_id().equals(l2.getUser_id()))
                                        {
                                            pDizi.add(l2);



                                        }

                                    }
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });




        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(mRef, Posts.class)
                .build();



        FirebaseRecyclerAdapter<Posts, MainActivity.FindPostsViewOrder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, MainActivity.FindPostsViewOrder>(options) {
                    @NonNull
                    @Override
                    public MainActivity.FindPostsViewOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_anasayfa_post_recycler_liste, parent, false);
                        MainActivity.FindPostsViewOrder viewHolder=new MainActivity.FindPostsViewOrder(view);
                        return viewHolder;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final MainActivity.FindPostsViewOrder holder, final int position, @NonNull final Posts model) {


                        holder.postPaylasanIsimSoyisim.setText(model.getPost_yukleyen_isim_soyisim());
                        holder.postAciklama.setText(model.getAciklama());
                        holder.postBegeniSayisi.setText(String.valueOf(model.getPost_begeni_sayisi())+" beğenme");
                        holder.postKacDkOnce.setText(String.valueOf(TimeAgo.getTimeAgo(model.getYuklenme_tarih())));


                        UniversalImagLoader.setImage(model.getPhoto_url(),holder.postImage,null,"");
                        UniversalImagLoader.setImage(model.getPost_yukleyen_profil_resmi(),holder.postPaylasanProfilResmi,null,"");


                        final String id=getRef(position).getKey();


                        ///


                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.child(id).hasChild(mAuth.getCurrentUser().getUid()))
                                {
                                    holder.begeniSayisi=(int)dataSnapshot.child(id).getChildrenCount();
                                    holder.postBegeniSayisi.setText(String.valueOf(holder.begeniSayisi)+" beğeni");
                                    Bitmap likeDoluKalp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_like_ok);
                                    holder.postLike.setImageBitmap(likeDoluKalp);

                                }
                                else
                                {
                                    holder.begeniSayisi=(int)dataSnapshot.child(id).getChildrenCount();
                                    holder.postBegeniSayisi.setText(String.valueOf(holder.begeniSayisi)+" beğeni");
                                    Bitmap likeBosKalp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_like);
                                    holder.postLike.setImageBitmap(likeBosKalp);
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        ///



                        holder.postLike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {



                                likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.child(id).hasChild(mAuth.getCurrentUser().getUid()))
                                        {


                                                likesRef.child(id).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                Bitmap likeBosKalp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_like);
                                                holder.postLike.setImageBitmap(likeBosKalp);

                                        }

                                        else{

                                                likesRef.child(id).child(mAuth.getCurrentUser().getUid()).setValue(true);
                                                Bitmap likeDoluKalp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_like_ok);
                                                holder.postLike.setImageBitmap(likeDoluKalp);


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



                            }
                        });


                       // Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();

                        ArrayList<ArkadasPost> aa=new ArrayList<>();

                        /*  mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()!=null)
                                {
                                    for(DataSnapshot arkadas:dataSnapshot.getChildren())
                                    {
                                        ArkadasPost arkadasPost=arkadas.getValue(ArkadasPost.class);



                                        if(model.getUser_id().equals(arkadasPost.getUser_arkadas_id()))
                                        {

                                        }
                                        else
                                        {
                                            notifyItemRemoved(position);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




                       /*

                        //Toast.makeText(SearchActivity.this, "d"+model.getUser_details().getProfile_picture(), Toast.LENGTH_SHORT).show();
                        UniversalImagLoader.setImage(model.getUser_details().getProfile_picture(),holder.myImage,null,"");
                        //  Picasso.with().load(model.getBiography()).placeholder(R.drawable.ic_person2).into(holder.myImage);


                        holder.myName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String id=getRef(position).getKey();
                               // Toast.makeText(SearchActivity.this, "id: "+id, Toast.LENGTH_SHORT).show();
                            }
                        });

                        */


                    }
                };
        postListe.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindPostsViewOrder extends RecyclerView.ViewHolder
    {
        CircleImageView postPaylasanProfilResmi;
        TextView postPaylasanIsimSoyisim, postBegeniSayisi, postKacDkOnce, postAciklama;
        ImageView postLike, postComment;
        GridImageView postImage;


        int begeniSayisi=0;





        public FindPostsViewOrder(@NonNull View itemView)
        {
            super(itemView);

            postPaylasanProfilResmi=itemView.findViewById(R.id.post_circle_profil_resmi);
            postPaylasanIsimSoyisim=itemView.findViewById(R.id.post_tvIsimSoyisim);
            postBegeniSayisi=itemView.findViewById(R.id.post_tvBegenmeSayisi);
            postKacDkOnce=itemView.findViewById(R.id.post_tvKacDkOnce);
            postAciklama=itemView.findViewById(R.id.post_tvAciklama);
            postLike=itemView.findViewById(R.id.post_imgLike);
            postComment=itemView.findViewById(R.id.post_imgComment);
            postImage=itemView.findViewById(R.id.post_imgPostFoto);


        }



    }


    private void UniversalInit() {
        UniversalImagLoader universalImageLoader = new UniversalImagLoader(getApplicationContext());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener!=null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
