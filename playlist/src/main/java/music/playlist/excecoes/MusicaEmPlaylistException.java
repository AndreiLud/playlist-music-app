package music.playlist.excecoes;

/**
 * Lançada ao tentar excluir uma música que ainda está associada a alguma playlist.
 */
public class MusicaEmPlaylistException extends RuntimeException {

    public MusicaEmPlaylistException(Integer id) {
        super("Música (id = " + id + ") não pode ser excluída pois está associada a uma ou mais playlists");
    }

}
