package com.hunorszondi.letstego.ui.chat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_contacts.*
import android.widget.EditText
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.model.apiModels.ContactModel
import com.hunorszondi.letstego.ui.chat.utils.ContactsRecyclerViewAdapter
import com.hunorszondi.letstego.utils.ResourceUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.ui.BaseFragment
import com.hunorszondi.letstego.ui.chat.utils.ContactRecyclerViewItemHelper
import com.hunorszondi.letstego.utils.DialogFactory
import com.hunorszondi.letstego.viewModels.ChatViewModel


/**
 * UI for contact management
 */
class ContactsFragment : BaseFragment(), ContactRecyclerViewItemHelper.RecyclerItemTouchHelperListener {

    companion object {
        fun newInstance() = ChatFragment()
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var contactListAdapter: ContactsRecyclerViewAdapter

    private val contactListObserver = Observer<MutableList<ContactModel>> { list ->
        val listToShow = list?: mutableListOf()
        if(listToShow.isEmpty()) {
            emptyListTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else{
            updateContactList(listToShow)
            emptyListTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ChatViewModel::class.java)
        navigationHeader.setTitleVisibility(View.VISIBLE)
            .setTitle(ResourceUtil.instance.getString(R.string.chats))
            .setButtonVisibility(View.VISIBLE)
            .setButtonIcon(Session.instance.currentUser!!.photo?:R.drawable.ic_settings_black_24dp)
            .setButtonClickListener(View.OnClickListener {
                if(view != null){
                    Navigation.findNavController(view!!).navigate(R.id.action_contactsFragment_to_profileFragment, null)
                }
            })

        viewModel.contacts.observe(this, contactListObserver)

        showLoading()
        viewModel.fetchContacts(::defaultCallback)

        initRecyclerView()

        addContactButton.setOnClickListener { addContact() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.exitContacts()
    }

    /**
     * Opens the chat with the selected contact
     */
    private fun onContactClicked(contact: ContactModel) {
        viewModel.currentContact = contact
        if(view != null){
            Navigation.findNavController(view!!).navigate(R.id.action_contactsFragment_to_chatFragment, null)
        }
    }

    /**
     * Initializing recycler view
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        contactListAdapter = ContactsRecyclerViewAdapter(ArrayList(), ::onContactClicked)
        val itemTouchHelperCallback = ContactRecyclerViewItemHelper(this, 0, ItemTouchHelper.LEFT)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = contactListAdapter
        }
    }

    /**
     * Updating contact list
     *
     * @param list to update with
     */
    private fun updateContactList(list: MutableList<ContactModel>) {
        contactListAdapter.updateList(list)
    }

    /**
     * Opens the dialog where the user can add a new contact
     */
    private fun addContact() {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.add_contact_dialog, null)

        val editText = dialogView.findViewById(R.id.edt_comment) as EditText
        val button1 = dialogView.findViewById(R.id.buttonSubmit) as Button
        val button2 = dialogView.findViewById(R.id.buttonCancel) as Button

        button2.setOnClickListener { dialogBuilder.dismiss() }
        button1.setOnClickListener {
            val userName = editText.text.toString()
            if(userName.isEmpty()) {
                Toast.makeText(context, "Enter a user name", Toast.LENGTH_SHORT).show()
            } else {
                showLoading()
                viewModel.addContact(editText.text.toString()) { status, message ->
                    requireActivity().runOnUiThread {
                        cancelLoading()
                        if(!status) {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        } else {
                            dialogBuilder.dismiss()
                        }
                    }
                }
            }
        }

        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    /**
     * Deletes a contact on swipe
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val dialog = DialogFactory.makeMessage(requireContext(),
            ResourceUtil.instance.getString(R.string.delete_contact),
            ResourceUtil.instance.getString(R.string.are_you_sure_delete_contact),
            ResourceUtil.instance.getString(R.string.yes),
            ResourceUtil.instance.getString(R.string.no),
            DialogInterface.OnClickListener { _, _ ->
                showLoading()
                viewModel.removeContactByPosition(position, ::defaultCallback)
            },
            DialogInterface.OnClickListener { _, _ -> contactListAdapter.notifyItemChanged(position) })
        dialog.show()
    }

    /**
     * Updates UI after backend api response if needed
     *
     * @param status positive or negative update
     * @param message status description
     */
    private fun defaultCallback(status: Boolean, message: String) {
        requireActivity().runOnUiThread {
            cancelLoading()
            if(!status) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
