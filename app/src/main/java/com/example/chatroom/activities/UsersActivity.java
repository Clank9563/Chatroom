package com.example.chatroom.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatroom.adapters.UsersAdapter;
import com.example.chatroom.databinding.ActivityUsersBinding;
import com.example.chatroom.listeners.UserListener;
import com.example.chatroom.models.User;
import com.example.chatroom.utilities.Constants;
import com.example.chatroom.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private  void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String curentUserId = preferenceManager.getstring(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();//建立user List
                        for (QueryDocumentSnapshot queryDocumentSnapshots : task.getResult()){
                            if (curentUserId.equals(queryDocumentSnapshots.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshots.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshots.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshots.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshots.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshots.getId();
                            users.add(user);
                        }
                        if (users.size() > 0 ){
                            UsersAdapter usersAdapter = new UsersAdapter(users,this);
                            binding.userRecycleView.setAdapter(usersAdapter);
                            binding.userRecycleView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void  showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void  loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onuserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER , user);
        startActivity(intent);
        finish();
    }
}