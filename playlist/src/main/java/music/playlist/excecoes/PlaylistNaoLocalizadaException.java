package music.playlist.excecoes;

public class PlaylistNaoLocalizadaException extends RuntimeException {

    public PlaylistNaoLocalizadaException(Integer playlistid) {
        super("Playlist não localizada (playlistid = " + playlistid + ")");
    }

}
