package ec.edu.unl.lojavents.user.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(length = 30)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion_propietario", nullable = false, length = 24)
    private EstadoVerificacionPropietario estadoVerificacionPropietario =
            EstadoVerificacionPropietario.NO_SOLICITADA;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private Set<Rol> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    protected Usuario() {
    }

    public Usuario(String nombres, String email, String passwordHash, String telefono) {
        this.nombres = nombres;
        this.email = email;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
        this.roles.add(Rol.CLIENTE);
    }

    public UUID getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public boolean isActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public EstadoVerificacionPropietario getEstadoVerificacionPropietario() {
        return estadoVerificacionPropietario;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public void actualizarPerfil(String nombres, String telefono) {
        this.nombres = nombres;
        this.telefono = telefono;
    }

    public void actualizarPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void cambiarEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public void actualizarVerificacionPropietario(EstadoVerificacionPropietario estado) {
        this.estadoVerificacionPropietario = estado;
    }

    public void agregarRol(Rol rol) {
        roles.add(rol);
        if (rol == Rol.PROPIETARIO) {
            estadoVerificacionPropietario = EstadoVerificacionPropietario.APROBADA;
        }
    }
}
