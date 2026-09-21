package music.playlist.excecoes;

public class MusicaNaoLocalizadaException extends RuntimeException {

    public MusicaNaoLocalizadaException(Integer id) {
        super("Música não localizada (id = " + id + ")");
    }

    public MusicaNaoLocalizadaException(Integer id, Integer playlistid) {
        super("Música (id = " + id + ") não localizada na playlist (playlistid = " + playlistid + ")");
    }

}
