package sound;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class MidiPlayer {
  private Synthesizer synth;
  private MidiChannel[] channels;
  private Instrument[] instruments;
  private Timer StopNoteTimer;
  private boolean StartedTimer;

  private HashMap<String, Integer> InstrumentsChannel = new HashMap<String, Integer>();
  private Vector<Integer> playedChannels = new Vector<Integer>();
  private Vector<Integer> playedNotes = new Vector<Integer>();

  public MidiPlayer() {

    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();
      channels = synth.getChannels();

      instruments = synth.getDefaultSoundbank().getInstruments();

      channels[0].programChange(instruments[11].getPatch().getProgram());
      channels[0].setMono(false);

      channels[1].programChange(instruments[106].getPatch().getProgram());
      channels[1].setMono(false);

      channels[2].programChange(instruments[44].getPatch().getProgram());
      channels[2].setMono(false);

      channels[2].programChange(instruments[44].getPatch().getProgram());
      channels[2].setMono(false);

      InstrumentsChannel.put("Xylophone", 0);
      InstrumentsChannel.put("Banjo", 1);
      InstrumentsChannel.put("Tuba", 2);
      InstrumentsChannel.put("Contrabass", 3);

    } catch (MidiUnavailableException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void stopNotes() {
    if (playedChannels.size() > 0) {
      channels[playedChannels.get(0)].noteOff(playedNotes.get(0));
      playedChannels.remove(0);
      playedNotes.remove(0);

      if (playedNotes.size() > 0) {
        StartedTimer = true;
        StopNoteTimer.schedule(
            new TimerTask() {
              @Override
              public void run() {
                stopNotes();
              }
            },
            500);
      } else {
        StartedTimer = false;
      }
    } else {
      StartedTimer = false;
    }
  }

  public void playNote(int instrument, int noteNr, int velocity) {

    int channelNr = 0;
    int startNote = 59;

    int maxNotes = 4;

    if (instrument == 138) {
      channelNr = InstrumentsChannel.get("Xylophone");
      //channels[channelNr].allNotesOff();
    } else if (instrument == 139) {
      startNote = 36;
      channelNr = InstrumentsChannel.get("Tuba");
      maxNotes = 1;
      //channels[channelNr].allNotesOff();
    } else if (instrument == 140) {
      startNote = 47;
      channelNr = InstrumentsChannel.get("Banjo");
      //channels[channelNr].allNotesOff();
    } else if (instrument == 140) {
      startNote = 36;
      channelNr = InstrumentsChannel.get("Contrabass");
      //channels[channelNr].allNotesOff();
    }

    if (playedNotes.size() > maxNotes) {
      channels[playedChannels.get(0)].noteOff(playedNotes.get(0));
      playedNotes.remove(0);
      playedChannels.remove(0);
    }

    channels[channelNr].noteOn(startNote + noteNr, velocity);

    playedChannels.add(channelNr);
    playedNotes.add(startNote + noteNr);

    if (!StartedTimer) {
      StartedTimer = true;
      StopNoteTimer = new Timer();
      StopNoteTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              stopNotes();
            }
          },
          1500);
    }

    /*
    channels[channelNr].allNotesOff();

    channels[channelNr].noteOn(noteNr, velocity);

    if(StartedTimer){
    	StopNoteTimer.cancel();
    	StopNoteTimer = new Timer();
    }

    StartedTimer = true;
    StopNoteTimer.schedule( new TimerTask(){
       	public void run() {
       		for(int i = 0; i < 10; i++){
       			channels[i].allNotesOff();
       		}
       	}
         }, 3000);


    channelNr++;


    if(channelNr > 10){
    	channelNr = 0;
    }
    */

  }

  public void stopAllNotes() {
    for (int i = 0; i < 3; i++) {
      channels[i].allNotesOff();
      channels[i].allSoundOff();
    }
    playedChannels.clear();
    playedNotes.clear();
  }
}
