package music.playlist.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import music.playlist.playlists.PlaylistEntity;

/**
 * Acesso ao microserviço playlists a partir do microserviço api.
 */
@FeignClient(name = "playlists", url = "${url}")
public interface PlaylistClienteFeign {

    @GetMapping("/playlists/{playlistid}")
    PlaylistEntity obterPlaylist(@PathVariable("playlistid") Integer playlistid);

    @PostMapping("/playlists/{playlistid}/musicas/{musicaId}")
    void adicionarMusica(@PathVariable("playlistid") Integer playlistid, @PathVariable("musicaId") Integer musicaId);

}
