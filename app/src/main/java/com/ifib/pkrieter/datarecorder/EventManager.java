package com.ifib.pkrieter.datarecorder;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private List<EventDefinition> eventList;
    private Context context;

    EventManager(Context context){
        this.context = context;
        this.eventList = new ArrayList<EventDefinition>();
        createEvents();
    }

    private void addEvent(EventDefinition event){
        eventList.add(event);
    }

    public List<EventDefinition> getEventList(){
        return eventList;
    }

    private void createEvents(){

        // 1 keyboard opened
        EventDefinition e1 = new EventDefinition("keyboard opened", true, this.context);
        e1.addFixedPosition("keyboard_small.jpg", 0,1168,1080,1640, false, true);
        e1.addFixedPosition("keyboard_small.jpg", 290,1670,360,1744, false, true);
        e1.addFixedPosition("keyboard_capital.jpg", 0,1168,1080,1640, false, true);
        e1.addFixedPosition("keyboard_numbers.jpg", 0,1168,1080,1640, false, true);
        e1.addFixedPosition("keyboard_numbers.jpg", 0,1670,130,1744, false, true);
        e1.addFixedPosition("keyboard_numbers_2.jpg", 0,1168,1080,1640, false, true);
        this.addEvent(e1);
        /*
        // AH
        // same as on pixel 2, same resolution

        // HS
        EventDefinition e1b = new EventDefinition("keyboard opened", true, this.context);
        e1b.addFixedPosition("whatsapp_hs.jpg", 0,872,720,1170, false, true);
        this.addEvent(e1b);


        // MD
        EventDefinition e1c = new EventDefinition("keyboard opened", true, this.context);
        e1c.addFixedPosition("whatsapp_md.jpg", 0,1900,1440,2520, false, true);
        this.addEvent(e1c);
        */
        /*
        // 2 SMS chat opened, with Person X
        EventDefinition e2 = new EventDefinition("SMS chat opened", false);
        e2.addFixedPosition("messages_chat.jpg", 40,65,100,205, false, true); //  go back arrow left
        e2.addFixedPosition("messages_chat.jpg", 860,65,1080,205, false, true); //  icons on the right in header
        e2.addsearchForText(180,85,750,158,"", false, true);
        this.addEvent(e2);

        // 3 SMS message/chatlist opened
        EventDefinition e3 = new EventDefinition("SMS message/chatlist opened", false);
        e3.addFixedPosition("messages.jpg", 0,65,1080,205, false, true);
        e3.addFixedPosition("messages.jpg", 919,1640,990,1710, false, true);
        //e3.addsearchForText(30,85,350,158,"Messages",false, false);
        this.addEvent(e3);

        // 4 SMS starting a new conversation screen
        EventDefinition e4 = new EventDefinition("SMS starting a new conversation screen", false);
        e4.addFixedPosition("new_conversation_screen.jpg", 40,65,100,205, false, true); //  go back arrow left
        e4.addsearchForText(180,85,750,158,"New conversation",false, false);
        this.addEvent(e4);

        // 5 Letter "k" got pressed
        EventDefinition e5 = new EventDefinition("Letter k got pressed", false);
        e5.addFixedPosition("keyboard_small.jpg", 0,1168,1080,1640, false, true);
        e5.addFixedPosition("new_conversation_screen.jpg", 815,1160,905,1460, false, true);
        this.addEvent(e5);

        // 6 whatsapp, chatlist opened
        EventDefinition e6 = new EventDefinition("whatsapp, chatlist opened", false);
        e6.addFixedPosition("whatsapp_list.jpg", 0,65,1080,327, false, true);
        e6.addFixedPosition("whatsapp_list.jpg", 919,1640,1015,1730, false, true);
        this.addEvent(e6);

        // 7 whatsapp chatlist, scrolled down
        EventDefinition e7 = new EventDefinition("whatsapp chatlist, scrolled down", false);
        e7.addFixedPosition("whatsapp_liste_scroll.jpg", 0,63,1080,188, false, true); //  header oben
        e7.addFixedPosition("whatsapp_list.jpg", 919,1640,1015,1730, false, true);
        this.addEvent(e7);

        // 8 whatsapp, chatlist active, unread messages
        EventDefinition e8 = new EventDefinition("whatsapp, chatlist active, number of chats with new messages", false);
        e8.addFixedPosition("whatsapp_list_newmessages.jpg", 105,242,430,336, false, true);
        e8.addsearchForText(321,258,350,288,"",true, false); //  get number in buble
        this.addEvent(e8);

        // 9 whatsapp chat opened with person X
        EventDefinition e9 = new EventDefinition("whatsapp chat opened", false, this.context);
        e9.addFixedPosition("whatsappchat.jpg", 750,65,1080,205, false, true); //  icons on the right in header
        e9.addFixedPosition("whatsappchat.jpg", 0,65,70,205, false, true); //  back arow in header
        e9.addsearchForText(180,85,750,158,"",false, true); //  get text with name from chat header
        this.addEvent(e9);
        */
        // AH
        EventDefinition e9a = new EventDefinition("whatsapp chat opened", false, this.context);
        e9a.addFixedPosition("whatsapp_ah.jpg", 750,65,1080,205, false, true); //  icons on the right in header
        e9a.addFixedPosition("whatsapp_ah.jpg", 0,65,70,205, false, true); //  back arow in header
        e9a.addsearchForText(175,65,750,170,"",false, true); //  get text with name from chat header
        this.addEvent(e9a);
        /*
        // HS
        EventDefinition e9b = new EventDefinition("whatsapp chat opened", false, this.context);
        e9b.addFixedPosition("whatsapp_hs.jpg", 470,50,720,155, false, true); //  icons on the right in header
        e9b.addFixedPosition("whatsapp_hs.jpg", 0,50,52,155, false, true); //  back arow in header
        e9b.addsearchForText(130,65,470,142,"",false, true); //  get text with name from chat header
        this.addEvent(e9b);

        // MD
        EventDefinition e9c = new EventDefinition("whatsapp chat opened", false, this.context);
        e9c.addFixedPosition("whatsapp_md.jpg", 1000,105,1440,288, false, true); //  icons on the right in header
        e9c.addFixedPosition("whatsapp_md.jpg", 0,105,90,288, false, true); //  back arow in header
        e9c.addsearchForText(230,105,1000,230,"",false, true); //  get text with name from chat header
        this.addEvent(e9c);
        */

        /*
        // 10 whatsapp, using camera
        EventDefinition e10 = new EventDefinition("whatsapp, using rear camera", false);
        e10.addFixedPosition("whatsapp_cam.jpg", 842,1600,902,1642, false, true);
        this.addEvent(e10);

        // 11 whatsapp button for taking picture/video pressed
        EventDefinition e11 = new EventDefinition("whatsapp, button for taking picture or video pressed", false);
        e11.addFixedPosition("whatsapp_cam3.jpg", 0,1880,50,1920, false, true);
        e11.addFixedPosition("whatsapp_cam3.jpg", 0,0,50,50, false, true);
        e11.addFixedPosition("whatsapp_cam3.jpg", 1030,0,1080,50, false, true);
        e11.addFixedPosition("whatsapp_cam3.jpg", 1030,1880,1080,1920, false, true);
        this.addEvent(e11);

        /*
        // 12 Instagram opened
        EventDefinition e12 = new EventDefinition("Instagram opened", false);
        e12.addFixedPosition("instagram_feed.jpg", 400,60,690,180, false, true);
        this.addEvent(e12);

        // 13 instagram, feed
        EventDefinition e13 = new EventDefinition("Instagram, feed opened", false);
        e13.addFixedPosition("instagram_feed.jpg", 400,60,690,180, false, true);
        this.addEvent(e13);

        // 14 instagram, feed, unread messages
        EventDefinition e14 = new EventDefinition("Instagram, feed opened, unread messages",false);
        e14.addFixedPosition("instagram_feed.jpg", 400,60,690,180, false, true);
        e14.addFixedPosition("instagram_feed.jpg", 950,60,1080,185, false, true);
        this.addEvent(e14);

        // 15 Instagram Directmessages Chat opened with:
        EventDefinition e15 = new EventDefinition("Instagram direct messages chat opened with",false);
        e15.addFixedPosition("instagram_directchat.jpg", 25,90,100,165, false, true);
        e15.addFixedPosition("instagram_directchat.jpg", 980,90,1055,165, false, true);
        e15.addsearchForText(110,90,970,165,"", false, true);
        this.addEvent(e15);
        */

        /*
        // 16 pinterest opened, chat with person X
        EventDefinition e16 = new EventDefinition("Pinterest opened, chat with",false);
        e16.addFixedPosition("pinterest_chat.jpg", 25,90,100,165, false, true);
        e16.addFixedPosition("pinterest_chat.jpg", 950,90,1030,165, false, true);
        e16.addsearchForText(110,90,940,165,"", false, true);
        this.addEvent(e16);

        // 17 BBC
        EventDefinition e17 = new EventDefinition("BBC news open, viewing top stories", false);
        e17.addFixedPosition("bbc_news_top_stories.jpg", 360,63,720,200, false, true);
        e17.addFixedPosition("bbc_news_top_stories.jpg", 0,230,305,336, false, true);
        e17.addsearchForText(0,230,305,326, "Top Stories", false, false);
        this.addEvent(e17);

        // 18 home screen present
        EventDefinition e18 = new EventDefinition("home screen present", true);
        e18.addFixedPosition("homescreen3.jpg", 96,1700,174,1777, false, true); //  the G in google searchbar at the bottom, dark
        e18.addFixedPosition("homescreen.jpg", 96,1700,174,1777, false, true); //  the G in google searchbar at the bottom, light
        this.addEvent(e18);

        // 19 app menu present
        EventDefinition e19 = new EventDefinition("app menu present", true);
        e19.addFixedPosition("appmenu.jpg", 95,120,175,200, false, true); //  the G in google searchbar at the top
        this.addEvent(e19);

        // 20 calculator present
        EventDefinition e20 = new EventDefinition("Calculator present", true);
        e20.addFixedPosition("calculator.jpg", 0,0,1080,1920, false, true);
        //e20.addFixedPosition("calculator.jpg", 0,700,1080,1790, true, true);
        this.addEvent(e20);

        // 21 lockscreen present
        EventDefinition e21 = new EventDefinition("lockscreen present", false);
        e21.addFixedPosition("lockscreen_2.jpg", 970,1810,1042,1878, true, true);
        this.addEvent(e21);

        // 22 lockscreen present, notifications are there
        EventDefinition e22 = new EventDefinition("lockscreen present, notifications are there ", false);
        e22.addFixedPosition("lockscreen_2.jpg", 970,1810,1042,1878, true, true);
        e22.addSearchForImage("lockscreen_2.jpg", 0,880,1080,930);
        this.addEvent(e22);

        // 23 Contacts, list opened
        EventDefinition e23 = new EventDefinition("Contacts, list opened", false);
        e23.addFixedPosition("contacts_list.jpg", 0,65,150,205, false, true);
        e23.addFixedPosition("contacts_list.jpg", 860,65,1080,205, false, true);
        e23.addsearchForText(150,65,750,205, "Contacts",false, false);
        this.addEvent(e23);

        // 24 Contacts, editing a contact
        EventDefinition e24 = new EventDefinition("Contacts, editing a contact", false);
        e24.addFixedPosition("contacts_edit.jpg", 0,65,1080,205, false, true);
        e24.addFixedPosition("contacts_edit.jpg", 0,65,150,205, false, true);
        e24.addFixedPosition("contacts_edit.jpg", 840,65,1080,205, false, true);
        this.addEvent(e24);

        // 25 calling, trying to calling person X
        EventDefinition e25 = new EventDefinition("calling, trying to call", false);
        e25.addFixedPosition("call_calling.jpg", 485,1600,596,1670, false, true);
        e25.addsearchForText(435,345,640,413, "Calling",false, false);
        e25.addsearchForText(0,420,1080,530, "", false, true); //  get name of caller
        this.addEvent(e25);

        // 26 calling, in call with
        EventDefinition e26 = new EventDefinition("calling, in call with", false);
        e26.addFixedPosition("call_calling.jpg", 485,1600,596,1670, false, true);
        e26.addsearchForText(485,1272,590,1345, "Hold",true, false); //  hold sign is only there if in call
        e26.addsearchForText(0,420,1080,530, "", false, true); //  get name of caller
        this.addEvent(e26);

        // 27 calling, hanging up, person X
        EventDefinition e27 = new EventDefinition("calling, hanging up", false);
        e27.addFixedPosition("call_hangingup.jpg", 485,1600,596,1670, false, true);
        e27.addsearchForText(0,544,1080,603, "Hanging",false, false);
        e27.addsearchForText(0,420,1080,530, "", false, true); //  get name of caller
        this.addEvent(e27);

        // 28 incoming call, lock screen
        EventDefinition e28 = new EventDefinition("incoming call lock screen", false);
        e28.addSearchForImage("call_incoming.jpg", 470,1500,600,1630);
        e28.addsearchForText(425,120,650,195, "Call from",false, false);
        e28.addsearchForText(0,200,1080,360, "", false, true); //  get name of caller
        this.addEvent(e28);

        // 29 Smiley keyboard opened
        EventDefinition e29 = new EventDefinition("Smiley keyboard opened", false);
        e29.addFixedPosition("keyboard_smiley.jpg", 294,1686,400,1784, false, true);
        this.addEvent(e29);

        // 30 home button pressed
        EventDefinition e30 = new EventDefinition("homebutton pressed", true);
        e30.addFixedPosition("homebutton.jpg", 492,1842,590,1871, true, true);
        e30.addFixedPosition("homebutton2.jpg", 488,1808,592,1904, true, true);
        e30.addFixedPosition("homebutton3.jpg", 488,1808,592,1904, true, true);
        e30.addFixedPosition("homebutton4.jpg", 488,1808,592,1904, true, true);

        e30.addFixedPosition("homebutton.jpg", 492,1842,590,1871, false, true);
        e30.addFixedPosition("homebutton2.jpg", 488,1808,592,1904, false, true);
        e30.addFixedPosition("homebutton3.jpg", 488,1808,592,1904, false, true);
        e30.addFixedPosition("homebutton4.jpg", 488,1808,592,1904, false, true);
        this.addEvent(e30);

        // updated insta events
        // 31 Instagram opened
        EventDefinition e31 = new EventDefinition("Instagram opened", false);
        e31.addFixedPosition("instagram_feed_v2.jpg", 100,65,355,185, false, true);
        this.addEvent(e31);

        // 32 instagram, feed
        EventDefinition e32 = new EventDefinition("Instagram, feed opened", false);
        e32.addFixedPosition("instagram_feed_v2.jpg", 100,65,355,185, false, true);
        this.addEvent(e32);

        // 33 instagram, feed, unread messages
        EventDefinition e33 = new EventDefinition("Instagram, feed opened, unread messages",false);
        e33.addFixedPosition("instagram_feed_v2.jpg", 100,65,355,185, false, true);
        e33.addFixedPosition("instagram_feed_v2.jpg", 950,60,1080,185, false, true);
        this.addEvent(e33);

        // 34 Instagram Directmessages Chat opened with:
        EventDefinition e34 = new EventDefinition("Instagram direct messages chat opened with",false);
        e34.addFixedPosition("instagram_directchat_v2.jpg", 25,90,100,165, false, true);
        e34.addFixedPosition("instagram_directchat_v2.jpg", 980,90,1055,165, false, true);
        e34.addsearchForText(300,180,800,235,"", false, true);
        this.addEvent(e34);
        */
    }
}
