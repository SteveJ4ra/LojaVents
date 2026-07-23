package ec.edu.unl.lojavents.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Embeddable
public class PeriodoReserva {

    public static final int DURACION_MINIMA_HORAS = 1;
    public static final int DURACION_MAXIMA_HORAS = 12;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "duracion_horas", nullable = false)
    private int duracionHoras;

    protected PeriodoReserva() {
    }

    public PeriodoReserva(LocalDate fecha, LocalTime horaInicio, int duracionHoras) {
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        this.horaInicio = Objects.requireNonNull(horaInicio, "La hora de inicio es obligatoria.");
        if (duracionHoras < DURACION_MINIMA_HORAS || duracionHoras > DURACION_MAXIMA_HORAS) {
            throw new IllegalArgumentException("La duracion debe estar entre 1 y 12 horas.");
        }
        this.duracionHoras = duracionHoras;
    }

    public LocalDateTime inicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    public LocalDateTime fin() {
        return inicio().plusHours(duracionHoras);
    }

    public boolean seSolapaCon(PeriodoReserva otro) {
        Objects.requireNonNull(otro, "El periodo comparado es obligatorio.");
        return inicio().isBefore(otro.fin()) && fin().isAfter(otro.inicio());
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public int getDuracionHoras() {
        return duracionHoras;
    }
}
