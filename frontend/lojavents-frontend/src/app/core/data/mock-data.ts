import { Booking } from '../../shared/models/booking.model';
import { Review } from '../../shared/models/review.model';
import { OwnerVerificationRequest, User } from '../../shared/models/user.model';
import { Venue } from '../../shared/models/venue.model';

export const MOCK_USERS: User[] = [
  {
    id: 'u-client',
    fullName: 'María Torres',
    email: 'cliente@lojavents.ec',
    phone: '0991112233',
    roles: ['CLIENTE'],
    status: 'ACTIVO',
    ownerVerificationStatus: 'NO_SOLICITADA',
    createdAt: '2026-04-10T10:00:00Z'
  },
  {
    id: 'u-owner',
    fullName: 'Carlos Jiménez',
    email: 'propietario@lojavents.ec',
    phone: '0982223344',
    roles: ['CLIENTE', 'PROPIETARIO'],
    status: 'ACTIVO',
    ownerVerificationStatus: 'APROBADA',
    createdAt: '2026-03-03T09:00:00Z'
  },
  {
    id: 'u-admin',
    fullName: 'Administración LojaVents',
    email: 'admin@lojavents.ec',
    phone: '0973334455',
    roles: ['ADMIN'],
    status: 'ACTIVO',
    ownerVerificationStatus: 'NO_SOLICITADA',
    createdAt: '2026-01-01T08:00:00Z'
  },
  {
    id: 'u-pending',
    fullName: 'Andrea Vega',
    email: 'andrea@example.com',
    phone: '0964445566',
    roles: ['CLIENTE'],
    status: 'ACTIVO',
    ownerVerificationStatus: 'PENDIENTE',
    createdAt: '2026-06-10T14:00:00Z'
  }
];

export const MOCK_CREDENTIALS: Record<string, string> = {
  'cliente@lojavents.ec': '123456',
  'propietario@lojavents.ec': '123456',
  'admin@lojavents.ec': '123456',
  'andrea@example.com': '123456'
};

export const MOCK_VENUES: Venue[] = [
  {
    id: 'venue-1',
    ownerId: 'u-owner',
    name: 'Jardín Mirador del Valle',
    shortDescription: 'Jardín amplio con vista panorámica y zona cubierta.',
    description: 'Un espacio rodeado de áreas verdes, ideal para bodas, cumpleaños y reuniones familiares. Incluye una zona cubierta, iluminación ambiental y estacionamiento privado.',
    neighborhood: 'El Valle',
    address: 'Av. Salvador Bustamante Celi, sector El Valle',
    pricePerHour: 65,
    capacity: 180,
    rating: 4.8,
    reviewCount: 34,
    eventTypes: ['Boda', 'Cumpleaños', 'Evento corporativo'],
    amenities: ['Estacionamiento', 'Wi-Fi', 'Cocina', 'Área verde', 'Sonido básico'],
    rules: ['No fumar dentro del área cubierta', 'Finalizar música amplificada hasta las 23:00', 'Respetar la capacidad máxima'],
    cancellationPolicy: 'Cancelación sin costo hasta 7 días antes. Después se retiene el 30% del valor.',
    images: ['/images/venue-1.svg', '/images/venue-1b.svg'],
    featured: true,
    active: true,
    blockedSlots: [
      { id: 'block-1', date: '2026-07-18', startTime: '10:00', endTime: '18:00', reason: 'Mantenimiento' }
    ]
  },
  {
    id: 'venue-2',
    ownerId: 'u-owner',
    name: 'Salón Colonial San Sebastián',
    shortDescription: 'Salón elegante en el centro histórico de Loja.',
    description: 'Arquitectura colonial restaurada, ambiente cálido y ubicación céntrica. Perfecto para recepciones, cenas privadas y encuentros culturales.',
    neighborhood: 'San Sebastián',
    address: 'Calle Bolívar y Lourdes',
    pricePerHour: 52,
    capacity: 95,
    rating: 4.6,
    reviewCount: 21,
    eventTypes: ['Boda', 'Cena', 'Evento cultural'],
    amenities: ['Wi-Fi', 'Proyector', 'Mobiliario', 'Cocina'],
    rules: ['No se permite pirotecnia', 'El montaje debe coordinarse con 24 horas de anticipación'],
    cancellationPolicy: 'Reembolso del 80% hasta 5 días antes.',
    images: ['/images/venue-2.svg', '/images/venue-2b.svg'],
    featured: true,
    active: true,
    blockedSlots: []
  },
  {
    id: 'venue-3',
    ownerId: 'u-owner',
    name: 'Terraza Altavista',
    shortDescription: 'Terraza moderna para celebraciones pequeñas y sesiones fotográficas.',
    description: 'Espacio moderno con iluminación decorativa, vista urbana y mobiliario modular. Recomendado para reuniones de hasta 60 personas.',
    neighborhood: 'Punzara',
    address: 'Av. Pío Jaramillo Alvarado, sector Punzara',
    pricePerHour: 42,
    capacity: 60,
    rating: 4.7,
    reviewCount: 18,
    eventTypes: ['Cumpleaños', 'Sesión fotográfica', 'Reunión'],
    amenities: ['Terraza', 'Wi-Fi', 'Mobiliario', 'Iluminación'],
    rules: ['No exceder el volumen permitido', 'No mover mobiliario sin autorización'],
    cancellationPolicy: 'Cancelación gratuita hasta 72 horas antes.',
    images: ['/images/venue-3.svg', '/images/venue-3b.svg'],
    featured: true,
    active: true,
    blockedSlots: []
  },
  {
    id: 'venue-4',
    ownerId: 'u-owner',
    name: 'Auditorio Ciudad de Loja',
    shortDescription: 'Auditorio equipado para conferencias y presentaciones.',
    description: 'Auditorio con escenario, proyector, sistema de audio y camerinos. Adecuado para conferencias, capacitaciones y eventos institucionales.',
    neighborhood: 'Centro',
    address: 'Calle 10 de Agosto y Bernardo Valdivieso',
    pricePerHour: 85,
    capacity: 260,
    rating: 4.5,
    reviewCount: 15,
    eventTypes: ['Conferencia', 'Concierto', 'Evento corporativo'],
    amenities: ['Escenario', 'Proyector', 'Sonido', 'Camerinos', 'Wi-Fi'],
    rules: ['Prueba técnica obligatoria', 'No ingresar alimentos al auditorio'],
    cancellationPolicy: 'Se requiere aviso con al menos 10 días de anticipación.',
    images: ['/images/venue-4.svg'],
    featured: false,
    active: true,
    blockedSlots: []
  },
  {
    id: 'venue-5',
    ownerId: 'u-owner',
    name: 'Casa Campestre Vilcabamba',
    shortDescription: 'Casa campestre para retiros y encuentros de fin de semana.',
    description: 'Propiedad tranquila con jardines, salón interior y área de parrillada. Ideal para reuniones familiares y actividades de bienestar.',
    neighborhood: 'Vilcabamba',
    address: 'Vía antigua a Yamburara',
    pricePerHour: 58,
    capacity: 120,
    rating: 4.9,
    reviewCount: 27,
    eventTypes: ['Retiro', 'Cumpleaños', 'Reunión familiar'],
    amenities: ['Área verde', 'Parrilla', 'Cocina', 'Estacionamiento'],
    rules: ['Cuidar las áreas verdes', 'No ingresar mascotas sin autorización'],
    cancellationPolicy: 'Reembolso completo hasta 7 días antes.',
    images: ['/images/venue-5.svg'],
    featured: false,
    active: true,
    blockedSlots: []
  },
  {
    id: 'venue-6',
    ownerId: 'u-owner',
    name: 'Estudio Creativo Norte',
    shortDescription: 'Estudio versátil para fotografía, talleres y contenido audiovisual.',
    description: 'Estudio con fondos, iluminación continua, zona de maquillaje y espacio adaptable para talleres creativos.',
    neighborhood: 'La Banda',
    address: 'Av. 8 de Diciembre, sector La Banda',
    pricePerHour: 30,
    capacity: 35,
    rating: 4.4,
    reviewCount: 12,
    eventTypes: ['Sesión fotográfica', 'Taller', 'Reunión'],
    amenities: ['Iluminación', 'Wi-Fi', 'Fondos fotográficos', 'Vestidor'],
    rules: ['Manipular equipos con supervisión', 'No consumir alimentos en la zona de fotografía'],
    cancellationPolicy: 'Cambio de fecha permitido hasta 24 horas antes.',
    images: ['/images/venue-6.svg'],
    featured: false,
    active: true,
    blockedSlots: []
  }
];

const now = new Date();
const futureDate = new Date(now.getFullYear(), now.getMonth() + 1, 15).toISOString().slice(0, 10);
const pastDate = new Date(now.getFullYear(), now.getMonth() - 1, 8).toISOString().slice(0, 10);

export const MOCK_BOOKINGS: Booking[] = [
  {
    id: 'booking-1',
    userId: 'u-client',
    venueId: 'venue-1',
    date: futureDate,
    startTime: '16:00',
    durationHours: 5,
    attendees: 80,
    billingAddress: { city: 'Loja', neighborhood: 'El Sagrario', street: 'Calle Sucre 12-34' },
    subtotal: 325,
    serviceFee: 26,
    total: 351,
    status: 'COMPLETADA',
    paymentReference: 'SIM-LOJA-1001',
    createdAt: '2026-06-12T16:00:00Z',
    reviewSubmitted: false
  },
  {
    id: 'booking-2',
    userId: 'u-client',
    venueId: 'venue-3',
    date: pastDate,
    startTime: '14:00',
    durationHours: 3,
    attendees: 25,
    billingAddress: { city: 'Loja', neighborhood: 'El Sagrario', street: 'Calle Sucre 12-34' },
    subtotal: 126,
    serviceFee: 10.08,
    total: 136.08,
    status: 'COMPLETADA',
    paymentReference: 'SIM-LOJA-0998',
    createdAt: '2026-05-01T11:00:00Z',
    reviewSubmitted: false
  }
];

export const MOCK_REVIEWS: Review[] = [
  {
    id: 'review-1',
    bookingId: 'external-1',
    venueId: 'venue-1',
    userId: 'external-user-1',
    userName: 'Lucía M.',
    rating: 5,
    comment: 'El jardín estuvo impecable y la vista fue perfecta para las fotografías.',
    createdAt: '2026-05-21T12:00:00Z'
  },
  {
    id: 'review-2',
    bookingId: 'external-2',
    venueId: 'venue-2',
    userId: 'external-user-2',
    userName: 'Diego P.',
    rating: 4,
    comment: 'Muy buena ubicación y atención. El salón tiene bastante personalidad.',
    createdAt: '2026-04-13T10:30:00Z'
  }
];

export const MOCK_OWNER_REQUESTS: OwnerVerificationRequest[] = [
  {
    userId: 'u-pending',
    identification: '1100000000',
    documentReference: 'documento-identidad-andrea.pdf',
    notes: 'Deseo publicar un salón familiar ubicado en el sector norte.',
    submittedAt: '2026-06-20T13:00:00Z'
  }
];
