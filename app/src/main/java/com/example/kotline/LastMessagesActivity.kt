package com.example.kotline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotline.databinding.ActivityLastMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class LastMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: RegistrationActivity.User? = null
    }
    private lateinit var binding: ActivityLastMessagesBinding
    //private lateinit var binding2: WriteNewMessageActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLastMessagesBinding.inflate(layoutInflater)  //activating viewBinding
        //enableEdgeToEdge()
        setContentView(binding.root)

        binding.LastMessagesHistoryRecyclerView.adapter = newAdapter
        binding.LastMessagesHistoryRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        newAdapter.setOnItemClickListener { item, view ->
            Log.d("MainScreen","clicked on user on main screen")
            val intent = Intent(this, MessageHistoryActivity::class.java)
            val row = item as LastMessagesRow
            intent.putExtra(WriteNewMessageActivity.key, row.chattersUser)
            startActivity(intent)
        }
        //setupDummyRows()
        listenForLastMessages()
        fetchCurrentUser()
        loggedInUserCheck()
    }

    val newAdapter = GroupAdapter<GroupieViewHolder>()

    class LastMessagesRow(val message:MessageHistoryActivity.Message): Item<GroupieViewHolder>(){
        var chattersUser: RegistrationActivity.User? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            val chattersId: String
            if (message.idFrom == FirebaseAuth.getInstance().uid){
                chattersId = message.idTo
                viewHolder.itemView.findViewById<TextView>(R.id.username_main_screen).text = message.idTo
            } else{
                chattersId = message.idFrom
            }
            val reference = FirebaseDatabase.getInstance().getReference("/users/$chattersId")
            reference.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chattersUser = snapshot.getValue(RegistrationActivity.User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.username_main_screen).text = chattersUser?.username
                    Picasso.get().load(chattersUser?.accountImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.pic_main_screen))

                }
                override fun onCancelled(error: DatabaseError) {}
            })


            viewHolder.itemView.findViewById<TextView>(R.id.message_main_screen).text = message.text

        }
        override fun getLayout(): Int {
            return  R.layout.message_history_main_screen
        }
    }


    val lastMessagesMap = HashMap<String, MessageHistoryActivity.Message>()

    private fun refreshLastMessagesHistoryRecyclerView(){
        val sortedMessages = lastMessagesMap.values.sortedByDescending { it.sendTime }
        newAdapter.clear()
        sortedMessages.forEach {
            newAdapter.add(LastMessagesRow(it))
        }
    }

    private fun listenForLastMessages(){
        val idFrom = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/last_messages/$idFrom")
        reference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(MessageHistoryActivity.Message::class.java) ?: return

                lastMessagesMap[snapshot.key!!] = message
                refreshLastMessagesHistoryRecyclerView()

                //newAdapter.add(LastMessagesRow(message))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(MessageHistoryActivity.Message::class.java) ?: return
                //newAdapter.add(LastMessagesRow(message))
                lastMessagesMap[snapshot.key!!] = message
                refreshLastMessagesHistoryRecyclerView()

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
//    //val newAdapter = GroupAdapter<GroupieViewHolder>()
//    private fun setupDummyRows(){
//        val newAdapter = GroupAdapter<GroupieViewHolder>()
//
//        //newAdapter.add(LastMessagesRow())
//        //newAdapter.add(LastMessagesRow())
//        //newAdapter.add(LastMessagesRow())
//
//        binding.LastMessagesHistoryRecyclerView.adapter = newAdapter
//    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        reference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(RegistrationActivity.User::class.java)
                Log.d("LatestMessages","Current user ${currentUser?.username}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun loggedInUserCheck() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.write_new_message -> {
                val intent = Intent(this, WriteNewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}