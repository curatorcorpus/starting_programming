import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class MusicPlayer {
    public static void main(String[] args){
	try{
	    AudioInputStream audioInputStream = 
		    	AudioSystem.getAudioInputStream(.getResource(
		    			"C:/Users/Jung Woo Park/Desktop/alarmMusic.mp3"));
	    Clip clip = AudioSystem.getClip();
	    clip.open(audioInputStream);
	    clip.start();
	} catch(Exception error){
	    System.out.println(error.getMessage());
	}
    }
}