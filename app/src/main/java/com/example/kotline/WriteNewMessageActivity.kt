package com.example.kotline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kotline.databinding.ActivityWriteNewMessageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class WriteNewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNewMessageBinding.inflate(layoutInflater)
        //enableEdgeToEdge()
        setContentView(binding.root)
        supportActionBar?.title = "Select user"

        val newAdapter = GroupAdapter<GroupieViewHolder>()
        //newAdapter.add(UserItem())
        //newAdapter.add(UserItem())
        //newAdapter.add(UserItem())
        binding.recyclerViewUsersListForNewMessage.adapter = newAdapter
        //binding.recyclerViewUsersListForNewMessage.layoutManager = LinearLayoutManager(this)

        usersFetching()
    }

    companion object{
        val key = "key"
    }

    private fun usersFetching(){

        val reference = FirebaseDatabase.getInstance().getReference("/users")
        val currentUserUid = FirebaseAuth.getInstance().uid
        reference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val newAdapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    Log.d("NewMessageActivity", it.toString())
                    val user = it.getValue(RegistrationActivity.User::class.java)
                    if (user != null && user.uid != currentUserUid) {
                        newAdapter.add(UserItem(user))
                    }
                }
                newAdapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, MessageHistoryActivity::class.java)
                    intent.putExtra(key, userItem.user)
                    startActivity(intent)
                    finish()
                }
                binding.recyclerViewUsersListForNewMessage.adapter = newAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("NewMessageActivity", "не палучилась")
            }
        })
    }
}

class UserItem(val user: RegistrationActivity.User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val usernameTextView = viewHolder.itemView.findViewById<TextView>(R.id.newMessageUsernametextView)
        usernameTextView.text = user.username
        Picasso.get().load(user.accountImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.newMessageAccountImageView))
    }

    override fun getLayout(): Int {
        return R.layout.write_new_message_users_row
    }
}

//class newAdapter: RecyclerView.Adapter<ViewHolder>{
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        TODO("Not yet implemented")
//    }
//}