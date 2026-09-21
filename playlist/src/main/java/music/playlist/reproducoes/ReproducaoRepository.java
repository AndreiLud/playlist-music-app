package music.playlist.reproducoes;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ReproducaoRepository extends CrudRepository<ReproducaoEntity, Integer> {

    List<ReproducaoEntity> findByPlaylistid(Integer playlistid);

    long countByPlaylistid(Integer playlistid);

}
