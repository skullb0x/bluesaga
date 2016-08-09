package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import game.BlueSaga;
import game.ClientSettings;
import graphics.BlueSagaColors;
import graphics.Font;
import graphics.ImageResource;
import network.Client;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;

import sound.Sfx;
import utils.RandomUtils;

public class Chat {

  private Image Chat_Bg;
  private Image Chat_Big_Bg;
  private Image Input_Bg;

  private static boolean BigChat = false;

  private Vector<String> special_type = new Vector<String>();

  private ArrayList<String> rows_author;
  private ArrayList<String> rows_text;
  private ArrayList<String> rows_type;

  private Vector<String> chatChannels = new Vector<String>();
  private HashMap<String, Color> chatChannelsColor = new HashMap<String, Color>();
  private Vector<String> chatChannelsToDelete = new Vector<String>();

  private HashMap<String, Color> chatColors = new HashMap<String, Color>();

  private HashMap<String, Integer> rows_count;
  private TextField input_field;

  private HashMap<String, Boolean> NewChatMessage = new HashMap<String, Boolean>();
  private int NewMessageBlinkItr = 0;

  private boolean Active;
  private String ActiveChatChannel;

  // Timer before you can announce again
  private int announce_cooldown_itr = 0;
  private int announce_cooldown_duration = 3600 * 1; // 5 minutes

  private static int X;
  private static int Y;

  private int Width = 407;

  private int opacity = 200;

  private int MAX_ROW_CHARS;

  private String lastInput;

  private boolean Visible;

  private Button SizeButton;

  public Chat(int newX, int newY, GameContainer app) {
    setVisible(true);

    X = newX;
    Y = newY;

    lastInput = "";

    Chat_Bg = ImageResource.getSprite("gui/chat/chat_bg").getImage();

    Chat_Big_Bg = ImageResource.getSprite("gui/chat/chat_big_bg").getImage();

    Input_Bg = ImageResource.getSprite("gui/chat/chat_input_bg").getImage();

    chatColors.put("announce", new Color(255, 243, 96));
    chatColors.put("local", new Color(255, 255, 255));
    chatColors.put("crew", new Color(200, 255, 111));
    chatColors.put("party", new Color(77, 184, 255));
    chatColors.put("private", new Color(255, 148, 61));
    chatColors.put("private_you", new Color(255, 148, 61, 150));

    special_type.add("event");
    special_type.add("error");

    input_field = new TextField(app, Font.size12, X + 15, Y + 165, 350, 30);
    input_field.setBackgroundColor(new Color(0, 0, 0, 0));
    input_field.setBorderColor(new Color(0, 0, 0, 0));
    input_field.setTextColor(new Color(255, 255, 255, 255));
    input_field.setFocus(false);
    input_field.setCursorVisible(true);
    input_field.setMaxLength(200);
    input_field.setAcceptingInput(true);

    SizeButton = new Button("", X, Y + 45, 40, 30, null);
    SizeButton.setVisible(false);
    SizeButton.getToolTip().setText("Expand Chat");

    reset();
  }

  public void draw(Graphics g, GameContainer app, int X, int Y) {
    // Announce cooldown
    if (announce_cooldown_itr > 0) {
      announce_cooldown_itr--;
    }

    for (Iterator<String> iterator = chatChannelsToDelete.iterator(); iterator.hasNext(); ) {
      String removeChannel = iterator.next();
      chatChannels.remove(removeChannel);
      rows_count.remove(removeChannel);
      NewChatMessage.remove(removeChannel);

      for (int i = 0; i < rows_type.size(); i++) {
        if (rows_type.get(i).equals(removeChannel)) {
          rows_type.remove(i);
          rows_text.remove(i);
          rows_author.remove(i);
          i--;
        }
      }
      iterator.remove();
      chatChannelsColor.remove(removeChannel);
    }

    if (rows_text.size() > 200) {
      // remove 100 old chat msgs when reaching over 500
      for (int i = 0; i < 100; i++) {
        int oldrowcount = rows_count.get(rows_type.get(i));
        rows_count.put(rows_type.get(i), oldrowcount - 1);
        rows_text.remove(i);
        rows_type.remove(i);
        rows_author.remove(i);
      }
    }

    if (isVisible()) {
      int drawY = Y;

      if (!Active) {
        drawY += 44;
      }

      int width = 400;
      int height = 150;

      // CHAT MAIN WINDOW
      Color chatColor = new Color(255, 243, 96, opacity);

      for (String channel : chatChannels) {
        if (ActiveChatChannel.equals(channel)) {
          chatColor = chatChannelsColor.get(channel);
        }
      }

      if (BigChat) {
        Chat_Big_Bg.draw(X, drawY - 340, chatColor);
      } else {
        Chat_Bg.draw(X, drawY, chatColor);
      }

      // CHAT INPUT FIELD
      if (Active) {
        Input_Bg.draw(X, drawY + height + 10, chatColor);
      }

      // CHAT CHANNEL MENU
      g.setFont(Font.size10);

      int tabY = Y + 25;

      if (Active) {
        tabY -= 40;
      }

      if (BigChat) {
        tabY -= 340;
      }

      if (NewMessageBlinkItr > 0) {
        NewMessageBlinkItr++;
      }

      int labelX = X;

      for (String channel : chatChannels) {
        if (ActiveChatChannel.equals(channel)) {
          g.setColor(new Color(0, 0, 0, 255));
          g.drawString(channel.toUpperCase(), labelX + 1, tabY + 1);
        }

        Color channelColor = new Color(255, 255, 255);
        if (chatChannelsColor.get(channel) != null) {
          channelColor =
              new Color(
                  chatChannelsColor.get(channel).getRed(),
                  chatChannelsColor.get(channel).getGreen(),
                  chatChannelsColor.get(channel).getBlue());
        }

        if (NewChatMessage.get(channel)) {
          if (NewMessageBlinkItr % 20 < 10) {
            g.setColor(channelColor);
          } else {
            g.setColor(
                new Color(
                    channelColor.getRed(), channelColor.getGreen(), channelColor.getBlue(), 100));
          }
        } else {
          if (ActiveChatChannel.equals(channel)) {
            g.setColor(channelColor);
          } else {
            g.setColor(
                new Color(
                    channelColor.getRed(), channelColor.getGreen(), channelColor.getBlue(), 200));
          }
        }

        g.drawString(channel.toUpperCase(), labelX, tabY);

        int labelWidth = Font.size10.getWidth(channel.toUpperCase());

        g.setColor(new Color(255, 255, 255, 100));
        g.drawString("|", labelX + labelWidth + 5, tabY);

        labelX += labelWidth + 15;
      }

      // CHAT TEXT CONTENT

      g.setColor(new Color(255, 255, 255, 255));

      int textpos = 0;

      if (BigChat) {
        g.setWorldClip(new Rectangle(X + 10, drawY + 15 - 340, width - 20, height - 20 + 340));
      } else {
        g.setWorldClip(new Rectangle(X + 10, drawY + 15, width - 20, height - 20));
      }

      int totalRows =
          rows_count.get(ActiveChatChannel) + rows_count.get("event") + rows_count.get("error");
      int startRow = totalRows - 30;

      if (startRow < 0) {
        startRow = 0;
      }

      int nrRow = 0;

      // DRAW CHAT TEXT
      for (int i = 0; i < rows_text.size(); i++) {
        boolean show = true;
        if (chatChannels.contains(rows_type.get(i))) {
          if (!rows_type.get(i).equals(ActiveChatChannel)) {
            show = false;
          }
        }

        if (show) {
          if (rows_type.get(i).equals("event") || rows_author.get(i).equals("event")) {
            g.setColor(new Color(130, 200, 245, 255));
          } else if (chatColors.containsKey(rows_type.get(i))) {
            g.setColor(chatColors.get(rows_type.get(i)));
          } else if (rows_author.get(i).equals("error")) {
            g.setColor(BlueSagaColors.RED);
          } else {
            g.setColor(chatChannelsColor.get(rows_type.get(i)));
          }
          g.setFont(Font.size12bold);
          textpos = X + 20;

          boolean same_author = false;
          // Draw > instead of name if same author as line before
          if (i > 0) {
            if (rows_author.get(i - 1) != null) {
              if (rows_author.get(i - 1) == rows_author.get(i)) {
                same_author = true;
              }
            }
          }

          if (!special_type.contains(rows_type.get(i))
              && !rows_author.get(i).equals("error")
              && !rows_author.get(i).equals("event")) {

            if (!same_author) {
              g.drawString(
                  rows_author.get(i) + ": ",
                  textpos,
                  drawY + height - 15 - (totalRows - nrRow) * 17);
              textpos = X + 30 + Font.size12bold.getWidth(rows_author.get(i) + ": ");
            } else {
              g.drawString("> ", textpos, drawY + height - 15 - (totalRows - nrRow) * 17);
              textpos = X + 35;
            }
            g.setFont(Font.size12);

            if (rows_author.get(i).equals(BlueSaga.playerCharacter.getName())) {
              g.setColor(new Color(255, 255, 255, 100));
            } else {
              g.setColor(new Color(255, 255, 255, 255));
            }
          }

          g.drawString(rows_text.get(i), textpos, drawY + height - 15 - (totalRows - nrRow) * 17);
          nrRow++;
        }
      }

      g.clearWorldClip();

      if (Active) {
        input_field.setLocation(X + 15, drawY + 168);
        g.setColor(new Color(255, 255, 255, 255));
        input_field.setTextColor(new Color(255, 255, 255, 255));
        input_field.render(app, g);
      }

      SizeButton.draw(g, BlueSaga.INPUT.getAbsoluteMouseX(), BlueSaga.INPUT.getAbsoluteMouseY());
    }
  }

  public void addTextLine(String chatType, String author, String newTextLine) {

    if (special_type.contains(chatType) || special_type.contains(author)) {
      MAX_ROW_CHARS = 43;
    } else {
      MAX_ROW_CHARS = 30;
    }

    if (newTextLine.length() > MAX_ROW_CHARS) {

      // REMOVE FIRST BLANK
      // TRY AND SPLIT THE ROW AT A BLANK
      formatTextRow(newTextLine, chatType, author);
    } else {
      rows_author.add(author);
      rows_type.add(chatType);
      rows_text.add(newTextLine);

      rows_count.put(chatType, rows_count.get(chatType) + 1);

      if (!special_type.contains(chatType) && !ActiveChatChannel.equals(chatType)) {
        NewChatMessage.put(chatType, true);
        NewMessageBlinkItr = 1;
      } else {
        NewChatMessage.put(chatType, false);
      }
    }
  }

  public void formatTextRow(String stringToFormat, String chatType, String author) {

    int rowEndIndex = 30;

    if (special_type.contains(chatType)) {
      rowEndIndex = 43;
    }

    int chatLines = 0;

    while (stringToFormat.length() > rowEndIndex) {

      rows_type.add(chatType);
      rows_count.put(chatType, rows_count.get(chatType) + 1);

      // remove first blank
      stringToFormat = removeFirstBlank(stringToFormat);

      // Try and cut the row at a space
      int charIndex = rowEndIndex;
      if (charIndex < stringToFormat.length()) {
        while (stringToFormat.charAt(charIndex) != ' ' && charIndex > 0) {
          charIndex--;
        }
      }

      // No spaces found, add whole row
      if (charIndex == 0) {
        charIndex = rowEndIndex;
      }

      rows_text.add(stringToFormat.substring(0, charIndex));
      stringToFormat = stringToFormat.substring(charIndex);

      //if first row show author name, otherwise just >
      if (chatLines == 0) {
        rows_author.add(author);
        // text rows can be longer on the following rows
        rowEndIndex = 40;

        if (special_type.contains(chatType)) {
          rowEndIndex = 43;
        }
      } else {
        rows_author.add(author);
      }

      chatLines++;
    }

    // Add the last row with rests of text
    if (chatLines > 0) {
      rows_type.add(chatType);
      rows_count.put(chatType, rows_count.get(chatType) + 1);

      rows_author.add(author);

      stringToFormat = removeFirstBlank(stringToFormat);

      rows_text.add(stringToFormat);
    }

    if (!special_type.contains(chatType)
        && !ActiveChatChannel.equals(chatType)
        && !chatType.equals("private")) {
      NewChatMessage.put(chatType, true);
      NewMessageBlinkItr = 1;
    }
  }

  public String removeFirstBlank(String stringToChange) {
    if (stringToChange.startsWith(" ")) {
      stringToChange = stringToChange.substring(1);
    }
    return stringToChange;
  }

  public void moveSpace() {}

  public void clearInputLine(String preset) {
    input_field.setText(preset);
  }

  public void clearInputLine() {
    input_field.setText("");
  }

  public String getInputText() {
    return input_field.getText();
  }

  public String getInputTextAsString() {
    return ActiveChatChannel + ";" + input_field.getText();
  }

  public void setInputText(String newText) {
    input_field.setText(newText);
    input_field.setCursorPos(newText.length());
  }

  public String getActiveChatChannel() {
    return ActiveChatChannel;
  }

  public void setActiveChatChannel(String activeChatChannel) {
    ActiveChatChannel = activeChatChannel;
  }

  public void keyLogic(Input INPUT, Client client) {

    if (INPUT.isKeyPressed(Input.KEY_ENTER)) {
      if (!isActive()) {
        setActive(true);
        input_field.setAcceptingInput(true);
        input_field.setCursorPos(0);
      } else {
        // CHECK IF ANYTHING WRITTEN, IF SO SEND IT TO SERVER
        String chatText = getInputText();
        lastInput = chatText;

        if (chatText.length() > 0) {

          // CHECK IF PRIVATE MESSAGE
          boolean sendOk = false;

          if (BlueSaga.playerCharacter.getAdminLevel() == 0
              && ActiveChatChannel.equals("announce")
              && announce_cooldown_itr > 0) {
            sendOk = false;
            Gui.addMessage("#messages.chat.wait_announce", BlueSagaColors.RED);
          } else {
            // SEND CHAT MESSAGE
            chatText = getInputTextAsString();

            sendOk = true;
            clearInputLine();
          }

          if (sendOk) {
            client.sendMessage("newchat", chatText);
            if (ActiveChatChannel.equals("announce")) {
              announce_cooldown_itr = announce_cooldown_duration;
            }
          }
        }
        setActive(false);
        input_field.setAcceptingInput(false);
      }
    }

    if (INPUT.isKeyPressed(Input.KEY_TAB)) {

      Sfx.playRandomPitch("gui/move_item");

      int chatChannelIndex = 0;

      for (String channel : chatChannels) {
        if (ActiveChatChannel.equals(channel)) {
          break;
        }
        chatChannelIndex++;
      }

      if (chatChannelIndex + 1 < chatChannels.size()) {
        chatChannelIndex++;
      } else {
        chatChannelIndex = 0;
      }

      ActiveChatChannel = chatChannels.get(chatChannelIndex);

      if (NewChatMessage.get(ActiveChatChannel)) {
        NewChatMessage.put(ActiveChatChannel, false);
      }

      for (String channel : chatChannels) {
        if (NewChatMessage.get(channel)) {
          NewMessageBlinkItr = 1;
        }
      }
      //toggleSize();
    }

    if (isActive()) {

      if (INPUT.isKeyPressed(Input.KEY_UP)) {
        input_field.setText(lastInput);
        input_field.setCursorPos(input_field.getText().length());
      } else if (INPUT.isKeyPressed(Input.KEY_DOWN)) {
        input_field.setText("");
        input_field.setCursorPos(0);
      }
    }
  }

  public boolean leftMouseClick(int mouseX, int mouseY) {
    boolean clickedWindow = false;

    if (isActive()) {
      if (mouseX > X && mouseX < X + Width && mouseY > 595 && mouseY < 630) {
        clickedWindow = true;
      }
    }

    if (SizeButton.isClicked(mouseX, mouseY)) {
      clickedWindow = true;
      if (BigChat) {
        BigChat = false;
        Sfx.play("gui/menu_no", 0.7f, 1.0f);
        clickedWindow = true;
        SizeButton.setY(Y + 45);
        SizeButton.getToolTip().setText("Expand Chat");
      } else if (!BigChat) {
        BigChat = true;
        Sfx.play("gui/menu_no", 1.3f, 1.0f);
        clickedWindow = true;
        SizeButton.setY(Y - 295);
        SizeButton.getToolTip().setText("Contract Chat");
      }
    }

    return clickedWindow;
  }

  public boolean isClickedOn(int mouseX, int mouseY) {
    boolean clickedChat = false;
    if (BigChat) {
      if (mouseX > X
          && mouseX < Width + 20
          && mouseY > Y - 295
          && mouseY < ClientSettings.SCREEN_HEIGHT - 20) {
        clickedChat = true;
      }

    } else if (!BigChat) {
      if (mouseX > X
          && mouseX < Width + 20
          && mouseY > Y
          && mouseY < ClientSettings.SCREEN_HEIGHT - 20) {
        clickedChat = true;
      }
    }
    return clickedChat;
  }

  public boolean isVisible() {
    return Visible;
  }

  public void setVisible(boolean visible) {
    Visible = visible;
  }

  public void unFocus() {
    input_field.setFocus(false);
  }

  public void addChatChannel(String type) {
    if (!chatChannels.contains(type)) {
      chatChannels.add(type);

      if (type.equals("crew")) {
        chatChannelsColor.put(type, new Color(200, 255, 111));
      } else if (type.equals("party")) {
        chatChannelsColor.put(type, new Color(77, 184, 255));
      } else {
        chatChannelsColor.put(
            type,
            new Color(
                RandomUtils.getInt(100, 255),
                RandomUtils.getInt(100, 255),
                RandomUtils.getInt(100, 255)));
      }

      rows_count.put(type, 0);
      if (!type.equals("crew")) {
        addTextLine(type, "event", "Type /quit to close channel");
      }
      NewChatMessage.put(type, false);
    }
  }

  public void reset() {
    chatChannels.clear();
    chatChannelsColor.clear();

    chatChannels.add("local");
    chatChannelsColor.put("local", new Color(255, 255, 255));

    chatChannels.add("announce");
    chatChannelsColor.put("announce", new Color(255, 243, 96));

    chatChannels.add("#world");
    chatChannelsColor.put("#world", new Color(94, 193, 239));

    rows_count = new HashMap<String, Integer>();
    rows_count.put("local", 0);
    rows_count.put("announce", 0);
    rows_count.put("#world", 0);
    rows_count.put("event", 0);
    rows_count.put("error", 0);

    rows_author = new ArrayList<String>();
    rows_author.clear();

    rows_text = new ArrayList<String>();
    rows_text.clear();

    rows_type = new ArrayList<String>();
    rows_type.clear();

    setActive(true);
    input_field.setAcceptingInput(true);
    input_field.setCursorPos(0);

    ActiveChatChannel = "local";

    addTextLine("event", "", "Press ENTER to type in the chat");
    addTextLine("event", "", "Press TAB to change channel");
    addTextLine("local", "event", "#channelname to create/join channel");
    addTextLine("local", "event", "@playername to send message");
    addTextLine("announce", "event", "Here you can announce something (1/min)");

    NewChatMessage.put("local", false);
    NewChatMessage.put("announce", false);
    NewChatMessage.put("#world", false);
  }

  public void removeChatChannel(String type) {
    if (chatChannels.contains(type)) {
      chatChannelsToDelete.add(type);

      if (ActiveChatChannel.equals(type)) {
        ActiveChatChannel = "local";
      }
    }
  }

  public void toggleSize() {
    if (BigChat) {
      BigChat = false;
    } else {
      BigChat = true;
    }
  }

  public boolean isActive() {
    return Active;
  }

  public void setActive(boolean activeState) {
    Active = activeState;
    input_field.setFocus(Active);
    input_field.setCursorVisible(true);
    input_field.setAcceptingInput(true);
  }
}
