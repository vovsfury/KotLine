package com.example.kotline

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kotline.databinding.ActivityMessageHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class MessageHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageHistoryBinding
    val newAdapter = GroupAdapter<GroupieViewHolder>()
    var userRight: RegistrationActivity.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageHistoryBinding.inflate(layoutInflater)
        //enableEdgeToEdge()
        setContentView(binding.root)

        binding.messagesHistoryRecyclerView.adapter = newAdapter

        userRight = intent.getParcelableExtra<RegistrationActivity.User>(WriteNewMessageActivity.key)
        supportActionBar?.title = userRight?.username
        //setupDummyData()
        listenForMessages()
        binding.sendMessageButton.setOnClickListener {
                Log.d("ChatLog", "Sending message...")
                sendingMessage()
                //listenForMessages()
        }
    }
//    private fun setupDummyData(){
//        val newAdapter = GroupAdapter<GroupieViewHolder>()
//
//        newAdapter.add(messagesLeftItem("бебра"))
//        newAdapter.add(messagesRightItem("лебра"))
//
//        binding.messagesHistoryRecyclerView.adapter = newAdapter
//
//        binding.sendMessageButton.setOnClickListener{
//            Log.d("ChatLog", "Sending message...")
//            sendingMessage()
//        }
//    }

    private fun listenForMessages(){
        val idFrom = FirebaseAuth.getInstance().uid
        val idTo = userRight?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$idFrom/$idTo")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    Log.d("ChatLog", message.text)

                    if (message.idFrom == FirebaseAuth.getInstance().uid){
                        val currentUser = LastMessagesActivity.currentUser
                        if (userRight != null) newAdapter.add(messagesRightItem(message.text, currentUser!!))
                    } else {
                        newAdapter.add(messagesLeftItem(message.text, userRight!!))
                    }
                    binding.messagesHistoryRecyclerView.scrollToPosition(newAdapter.itemCount - 1)
                }
                else {
                    Log.d("ChatLog", "Message is null")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    class Message(val id: String, val text: String, val idFrom: String, val idTo: String, val sendTime: Long){
        constructor() : this ("", "", "", "",-1)
    }

    private fun sendingMessage(){
        //val newAdapter = GroupAdapter<GroupieViewHolder>()
        //binding.messagesHistoryRecyclerView.adapter = newAdapter
        val text = binding.newMessageTextTextView.text.toString()
        val user = intent.getParcelableExtra<RegistrationActivity.User>(WriteNewMessageActivity.key)
        val idTo = user?.uid
        val idFrom = FirebaseAuth.getInstance().uid

        if (idFrom == null || idTo == null) return

        //val ref = FirebaseDatabase.getInstance().getReference("/user_messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$idFrom/$idTo").push()
        val refTo = FirebaseDatabase.getInstance().getReference("/user_messages/$idTo/$idFrom").push()
        val message = Message(ref.key!!, text, idFrom, idTo, System.currentTimeMillis()/1000)
        ref.setValue(message)
            .addOnSuccessListener {
                Log.d("ChatLog", "${ref.key}, $text, $idFrom, $idTo, ${System.currentTimeMillis()/1000}")
                binding.newMessageTextTextView.text.clear()
                binding.messagesHistoryRecyclerView.scrollToPosition(newAdapter.itemCount)
            }
        refTo.setValue(message)

        val lastMessagesReference = FirebaseDatabase.getInstance().getReference("/last_messages/$idFrom/$idTo")
        lastMessagesReference.setValue(message)

        val lastMessagesToReference = FirebaseDatabase.getInstance().getReference("/last_messages/$idTo/$idFrom")
        lastMessagesToReference.setValue(message)
    }
}


class messagesRightItem(val text: String, val user: RegistrationActivity.User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.got_message).text = text
        Picasso.get().load(user.accountImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.account_image))
    }
    override fun getLayout(): Int {
        return R.layout.messages_row_left
    }
}

class messagesLeftItem(val text: String, val user: RegistrationActivity.User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.sent_message).text = text
        Picasso.get().load(user.accountImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.account_image_own))
    }
    override fun getLayout(): Int {
        return R.layout.messages_row_right
    }
}