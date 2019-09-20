package com.example.mychattingapp.messeges

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mychattingapp.R
import com.example.mychattingapp.models.ChatMessage
import com.example.mychattingapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG ="ChatLog"
    }
    val adapter = GroupAdapter<ViewHolder>()
    var toUser : User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter=adapter


        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser= intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title=toUser?.username
        //setupDummyData()
        listenForMessage()
        send_button_chatlog.setOnClickListener {
            Log.d(TAG, "Attemp To send MEssage")
            performSendMesssage()
  }
    }
    private fun listenForMessage(){
        val fromId= FirebaseAuth.getInstance().uid
        val toId=toUser?.uid
        val ref =FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=  p0.getValue(ChatMessage::class.java)
                if(chatMessage!=null){
                    Log.d(TAG,chatMessage.text)
                    if (chatMessage.fromId==FirebaseAuth.getInstance().uid){
                        val currentUser=LatestMessegesActivity.currentUser ?: return
                        adapter.add(ChatFromIteam(chatMessage.text,currentUser))
                    }else {
                        adapter.add(ChatToIteam(chatMessage.text,toUser!!))
                    }
                }
             }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
        })
    }
    private fun performSendMesssage() {
        val text = edittext_chatlog.text.toString()
        val fromId= FirebaseAuth.getInstance().uid
        if (fromId==null)return
        val user= intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId=user.uid
        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!,text,fromId,toId,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved Our Chat message ${reference.key})")
                edittext_chatlog.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }
        toReference.setValue(chatMessage)

        val latestMessageReference= FirebaseDatabase.getInstance().getReference("latest-messages/$fromId,/$toId")
            latestMessageReference.setValue(chatMessage)
        val latestMessageToReference= FirebaseDatabase.getInstance().getReference("latest-messages/$toId,/$fromId")
            latestMessageToReference.setValue(chatMessage)
    }

}
class ChatFromIteam (val text1: String,val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
    viewHolder.itemView.text_view_from.text= text1
        val uri=user.profileImageUrl
        val targetImageView = viewHolder.itemView.image_view_chat_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
    return R.layout.chat_from_row
    }
}
class ChatToIteam (val text2: String,val user:User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_view_to.text= text2
        val uri=user.profileImageUrl
        val targetImageView = viewHolder.itemView.image_view_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

