package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserv.R;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private DBHelper db;
    private UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);
        db = new DBHelper(this);
        User user = db.getUserById(new SessionManager(this).getUserId());
        if (user == null || !Roles.ADMIN.equals(user.role)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        adapter = new UsersAdapter(new ArrayList<>(), (u, block) -> {
            db.setUserBlocked(u.id, block);
            Toast.makeText(this, block ? "User blocked." : "User unblocked.", Toast.LENGTH_SHORT).show();
            load();
        });

        RecyclerView rv = findViewById(R.id.rvUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        load();
    }

    private void load() {
        adapter.updateData(db.getAllUsers());
    }

    // ---- Adapter ----

    interface OnToggleBlock {
        void onToggle(User user, boolean block);
    }

    class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserVH> {
        private List<User> users;
        private final OnToggleBlock onToggleBlock;

        UsersAdapter(List<User> users, OnToggleBlock onToggleBlock) {
            this.users = users;
            this.onToggleBlock = onToggleBlock;
        }

        void updateData(List<User> newList) {
            users = newList;
            notifyDataSetChanged();
        }

        @Override
        public UserVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserVH(view);
        }

        @Override
        public void onBindViewHolder(UserVH holder, int position) {
            holder.bind(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserVH extends RecyclerView.ViewHolder {
            final TextView tvName, tvPhone, tvRole, tvStatus;
            final MaterialButton btnToggle;

            UserVH(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvUserName);
                tvPhone = itemView.findViewById(R.id.tvUserPhone);
                tvRole = itemView.findViewById(R.id.tvUserRole);
                tvStatus = itemView.findViewById(R.id.tvUserStatus);
                btnToggle = itemView.findViewById(R.id.btnToggleBlock);
            }

            void bind(User user) {
                tvName.setText(user.name);
                tvPhone.setText(user.phone);
                tvRole.setText(user.role);
                if (user.isBlocked) {
                    tvStatus.setText("Blocked");
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_active));
                    btnToggle.setText("Unblock");
                    btnToggle.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.primary_green));
                } else {
                    tvStatus.setText("Active");
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_completed));
                    btnToggle.setText("Block");
                    btnToggle.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.status_active));
                }
                btnToggle.setOnClickListener(v -> onToggleBlock.onToggle(user, !user.isBlocked));
            }
        }
    }
}
