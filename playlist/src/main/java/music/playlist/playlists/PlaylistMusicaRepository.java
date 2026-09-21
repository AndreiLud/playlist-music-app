package music.playlist.playlists;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PlaylistMusicaRepository extends CrudRepository<PlaylistMusicaEntity, Integer> {

    Optional<PlaylistMusicaEntity> findByPlaylistidAndMusicaid(Integer playlistid, Integer musicaid);

    /** Somente os ids das músicas, sem carregar as entidades de associação. */
    @Query("select pm.musicaid from PlaylistMusicaEntity pm where pm.playlistid = ?1 order by pm.id")
    List<Integer> listarMusicaIds(Integer playlistid);

    /** Exclusão em bloco: um único DELETE, sem carregar as entidades antes. */
    @Transactional
    @Modifying
    @Query("delete from PlaylistMusicaEntity pm where pm.playlistid = ?1")
    void removerTodasDaPlaylist(Integer playlistid);

    /** Retorna a quantidade de associações removidas (0 = a música não estava na playlist). */
    @Transactional
    @Modifying
    @Query("delete from PlaylistMusicaEntity pm where pm.playlistid = ?1 and pm.musicaid = ?2")
    int removerDaPlaylist(Integer playlistid, Integer musicaid);

}
