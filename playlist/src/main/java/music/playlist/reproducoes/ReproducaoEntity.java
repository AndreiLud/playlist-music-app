package music.playlist.reproducoes;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "REPRODUCOES")
public class ReproducaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "playlistid é obrigatório")
    @Column(name = "PLAYLISTID")
    private Integer playlistid;

    @Column(name = "DATAHORA")
    private LocalDateTime datahora;

    public ReproducaoEntity() {
    }

    public ReproducaoEntity(Integer playlistid) {
        this.playlistid = playlistid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlaylistid() {
        return playlistid;
    }

    public void setPlaylistid(Integer playlistid) {
        this.playlistid = playlistid;
    }

    public LocalDateTime getDatahora() {
        return datahora;
    }

    public void setDatahora(LocalDateTime datahora) {
        this.datahora = datahora;
    }

}
