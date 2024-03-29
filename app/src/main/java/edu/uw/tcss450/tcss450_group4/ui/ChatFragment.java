package edu.uw.tcss450.tcss450_group4.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uw.tcss450.tcss450_group4.MobileNavigationDirections;
import edu.uw.tcss450.tcss450_group4.R;
import edu.uw.tcss450.tcss450_group4.model.Chat;
import edu.uw.tcss450.tcss450_group4.model.ChatMessageNotification;
import edu.uw.tcss450.tcss450_group4.model.ConnectionItem;
import edu.uw.tcss450.tcss450_group4.model.Message;
import edu.uw.tcss450.tcss450_group4.utils.SendPostAsyncTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static edu.uw.tcss450.tcss450_group4.R.id.layout_homeActivity_wait;
import static edu.uw.tcss450.tcss450_group4.R.id.nav_host_fragment;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_base_url;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_chats;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_connection;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_getall;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_messaging_base;
import static edu.uw.tcss450.tcss450_group4.R.string.ep_messaging_getAll;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_connections;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_firstname;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_image;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_lastname;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_memberid;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_connection_username;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_login_success;
import static edu.uw.tcss450.tcss450_group4.R.string.keys_json_messaging_success;

/**
 * This class used to display all the chats and the most recent message of that chat.
 *
 * @author Chinh Le
 * @version Nov 1 2019
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private List<Chat> mChats;
    private int mMemberId;
    private String mJwToken;
    private String mEmail;
    private ArrayList<Message> mMessageList;
    private Chat mChat;
    private ChatMessageNotification mChatMessage;
    private String mChatId;
    private String mChatName;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChatFragment newInstance(int columnCount) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create. Sets the arguments
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("ViewChat", getArguments() + "");

        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(getArguments());
        mChats = new ArrayList<>(Arrays.asList(args.getChats()));
        mMemberId = args.getMemberId();
        mJwToken = args.getJwt();
//        mEmail = args.getEmail();
        mChatMessage = args.getChatMessage();
    }

    /**
     * On Create View
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        TextView txt = view.findViewById(R.id.txt_display_no_chat);
        if(mChats.size() != 0) {
            txt.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    /**
     * On View Created. Sets up the chat list
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.chatList);
        ImageButton btnCreateChat = view.findViewById(R.id.button_create_chat);
        MyChatRecyclerViewAdapter mChatAdapter = new MyChatRecyclerViewAdapter(mChats, chat -> displayChat(chat.getChatId()));
        // Set the adapter
        if (rv instanceof RecyclerView) {
            Context context = rv.getContext();
            RecyclerView recyclerView = rv;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mChatAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(mChatAdapter);
        }
        btnCreateChat.setOnClickListener(this::onClick);
        if (mChatMessage != null) {

            displayChat(mChatMessage.getChatId());
            mChatMessage = null;

        }
    }

    /**
     * On Resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * When create chat is clicked, loads connections
     * @param v
     */
    @Override
    public void onClick(View v) {
        gotoConnection();
        MyCreateChatRecyclerViewAdapter.getFriendIDList().clear();
    }

    /**
     * Goes to connection to fetch connections list
     */
    private void gotoConnection() {
        Uri uriConnection = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(ep_base_url))
                .appendPath(getString(ep_connection))
                .appendPath(getString(ep_getall))
                .build();
        JSONObject msgBody = new JSONObject();
        try{
            msgBody.put("memberId", getmMemberId());
        } catch (JSONException e) {
            Log.wtf("MEMBERID", "Error creating JSON: " + e.getMessage());

        }
        new SendPostAsyncTask.Builder(uriConnection.toString(), msgBody)
                .onPostExecute(this::handleConnectionOnPostExecute)
                .onCancelled(error -> Log.e("CONNECTION FRAG", error))
                .addHeaderField("authorization", mJwToken)  //add the JWT as header
                .build().execute();

    }

    /**
     * Handles connections on post execution of async task
     * @param result
     */
    private void handleConnectionOnPostExecute(final String result) {
        //parse JSON
        try {
            boolean hasConnection = false;
            JSONObject root = new JSONObject(result);
            if (root.has(getString(keys_json_connection_connections))){
                hasConnection = true;
            } else {
                Log.e("ERROR!", "No connection");
            }

            if (hasConnection){
                JSONArray connectionJArray = root.getJSONArray(
                        getString(keys_json_connection_connections));
                ConnectionItem[] conItem = new ConnectionItem[connectionJArray.length()];
                for(int i = 0; i < connectionJArray.length(); i++){
                    JSONObject jsonConnection = connectionJArray.getJSONObject(i);
                    conItem[i] = new ConnectionItem(
                            jsonConnection.getInt(
                                    getString(keys_json_connection_memberid))
                            , jsonConnection.getString(
                            getString(keys_json_connection_firstname))
                            , jsonConnection.getString(
                            getString(keys_json_connection_lastname))
                            ,jsonConnection.getString(
                            getString(keys_json_connection_username))
                            ,jsonConnection.getString(
                            getString(keys_json_connection_image))
                    );
                }

                MobileNavigationDirections.ActionGlobalNavCreateChat directions
                        = CreateChatFragmentDirections.actionGlobalNavCreateChat(conItem);
                directions.setJwt(mJwToken);
//                directions.setEmail(mEmail);
                directions.setMemberId(getmMemberId());

                Navigation.findNavController(getActivity(), nav_host_fragment)
                        .navigate(directions);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays all chats user is a part of
     * @param chatId
     */
    private void displayChat(final String chatId){

        mChatId = chatId;
        JSONObject msgBody = new JSONObject();
        try {
            msgBody.put("chatId", chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uriChats = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(ep_base_url))
                .appendPath(getString(ep_messaging_base))
                .appendPath(getString(ep_messaging_getAll))
                .build();
        new SendPostAsyncTask.Builder(uriChats.toString(), msgBody)
                .onPostExecute(this::handleMessageGetOnPostExecute)
                .addHeaderField("authorization", mJwToken)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    /**
     * Handles messages received after successful async task
     * @param result
     */
    private void handleMessageGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean(getString(keys_json_messaging_success))) {
                JSONArray data = root.getJSONArray("messages");
                Message[] messages = new Message[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonChatLists = data.getJSONObject(i);

                    messages[i] = (new Message.Builder(jsonChatLists.getString("username"),
                                jsonChatLists.getInt("memberid"),
                                jsonChatLists.getString("message"),
                                convertTimeStampToDate(jsonChatLists.getString("timestamp")),
                            jsonChatLists.getString("profileuri"))
                                .build());
                }
                mChatName = root.getString("chatname");
                MobileNavigationDirections.ActionGlobalNavViewChat directions;
                directions = ViewChatFragmentDirections.actionGlobalNavViewChat(messages);
                directions.setMemberId(mMemberId);
                directions.setJwt(mJwToken);
                directions.setChatId(mChatId);
                directions.setChatName(mChatName);
                Navigation.findNavController(getActivity(), nav_host_fragment).navigate(directions);

            } else {
                Log.e("ERROR!", "No response");
            }
            getActivity().findViewById(layout_homeActivity_wait).setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
            getActivity().findViewById(layout_homeActivity_wait).setVisibility(View.GONE);
            Log.e("ERROR!", e.getMessage());
        }
    }

    /**
     * Handle errors if async task fails
     * @param result
     */
    private void handleErrorsInTask(final String result) {
        getActivity().findViewById(layout_homeActivity_wait).setVisibility(View.GONE);
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Convert timestamp to human-readable format
     * @param timestamp
     * @return time and date
     */
    private String convertTimeStampToDate(String timestamp) {
        Date date = new Date();
        String a = "";
        //Date showTime = new Date();
        //Date showDate = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        DateFormat daysFormat = new SimpleDateFormat("MMM dd yyyy hh:mm a");
        //DateFormat dateFormat = new SimpleDateFormat("MM-dd");
        try {
            date = format.parse(timestamp);
            a = daysFormat.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return a;
    }

    /**
     * Get memberId
     * @return memberId
     */
    public int getmMemberId() {
        return mMemberId;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Chat item);
    }
}
