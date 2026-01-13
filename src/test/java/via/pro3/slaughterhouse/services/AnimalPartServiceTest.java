package via.pro3.slaughterhouse.services;

import com.slaughterhouse.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import via.pro3.slaughterhouse.model.AnimalPart;
import via.pro3.slaughterhouse.repositories.AnimalPartRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnimalPartServiceTest {

    @Mock
    private AnimalPartRepository partRepository;

    @InjectMocks
    private AnimalPartService animalPartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- CreateAnimalPart ----------
    @Test
    void createAnimalPart_ShouldReturnCreatedPart() {
        CreateAnimalPartRequest request = CreateAnimalPartRequest.newBuilder()
            .setPart(AnimalPartProto.newBuilder()
                .setId(1)
                .setAnimalId(101)
                .setWeight(22.5)
                .setPartType("Leg")
                .build())
            .build();

        AnimalPart savedEntity = new AnimalPart();
        savedEntity.setId(1);
        savedEntity.setAnimalId(101);
        savedEntity.setWeight(22.5);
        savedEntity.setPartType("Leg");

        when(partRepository.save(any(AnimalPart.class))).thenReturn(savedEntity);

        AtomicReference<CreateAnimalPartResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.createAnimalPart(request, new StreamObserver<>() {
            @Override
            public void onNext(CreateAnimalPartResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertNull(errorRef.get());
        CreateAnimalPartResponse response = responseRef.get();
        assertNotNull(response);
        assertEquals("Leg", response.getPart().getPartType());
        assertEquals(101, response.getPart().getAnimalId());
        verify(partRepository).save(any(AnimalPart.class));
    }

    // ---------- ListAnimalParts ----------
    @Test
    void listAnimalParts_ShouldReturnAllParts() {
        AnimalPart p1 = new AnimalPart();
        p1.setId(1);
        p1.setAnimalId(100);
        p1.setPartType("Leg");
        p1.setWeight(25.0);

        AnimalPart p2 = new AnimalPart();
        p2.setId(2);
        p2.setAnimalId(200);
        p2.setPartType("Tail");
        p2.setWeight(5.0);

        when(partRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        AtomicReference<ListAnimalPartsResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.listAnimalParts(com.google.protobuf.Empty.getDefaultInstance()
            , new StreamObserver<>() {
            @Override
            public void onNext(ListAnimalPartsResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertNull(errorRef.get());
        ListAnimalPartsResponse response = responseRef.get();
        assertEquals(2, response.getPartsCount());
        assertEquals("Leg", response.getParts(0).getPartType());
        assertEquals("Tail", response.getParts(1).getPartType());
        verify(partRepository).findAll();
    }

    // ---------- GetAnimalPart ----------
    @Test
    void getAnimalPart_ShouldReturnPart() {
        AnimalPart part = new AnimalPart();
        part.setId(10);
        part.setAnimalId(999);
        part.setPartType("Wing");
        part.setWeight(12.0);

        when(partRepository.findById(10)).thenReturn(Optional.of(part));

        AtomicReference<AnimalPartProto> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.getAnimalPart(GetByIdRequest.newBuilder().setId(10).build(), new StreamObserver<>() {
            @Override
            public void onNext(AnimalPartProto value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertNull(errorRef.get());
        AnimalPartProto result = responseRef.get();
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Wing", result.getPartType());
        assertEquals(12.0, result.getWeight());
    }

    @Test
    void getAnimalPart_ShouldReturnErrorIfNotFound() {
        when(partRepository.findById(55)).thenReturn(Optional.empty());

        AtomicReference<AnimalPartProto> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.getAnimalPart(GetByIdRequest.newBuilder().setId(55).build(), new StreamObserver<>() {
            @Override
            public void onNext(AnimalPartProto value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertNull(responseRef.get());
        assertNotNull(errorRef.get()); // Should have error
    }

    // ---------- DeleteAnimalPart ----------
    @Test
    void deleteAnimalPart_ShouldDeleteIfExists() {
        when(partRepository.existsById(5)).thenReturn(true);

        AtomicReference<DeleteResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.deleteAnimalPart(DeleteByIdRequest.newBuilder().setId(5).build(), new StreamObserver<>() {
            @Override
            public void onNext(DeleteResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertNull(errorRef.get());
        DeleteResponse response = responseRef.get();
        assertFalse(response.hasError());
        verify(partRepository).deleteById(5);
    }

    @Test
    void deleteAnimalPart_ShouldReturnErrorIfNotFound() {
        when(partRepository.existsById(77)).thenReturn(false);

        AtomicReference<DeleteResponse> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        animalPartService.deleteAnimalPart(
            DeleteByIdRequest.newBuilder().setId(77).build(),
            new StreamObserver<>() {
                @Override
                public void onNext(DeleteResponse value) {
                    responseRef.set(value);
                }

                @Override
                public void onError(Throwable t) {
                    errorRef.set(t);
                }

                @Override
                public void onCompleted() {}
            }
        );

        // Since gRPC returns error, the response should be null
        assertNull(responseRef.get());
        assertNotNull(errorRef.get());
        assertTrue(errorRef.get().getMessage().contains("AnimalPart with ID 77 not found"));

        verify(partRepository, never()).deleteById(anyInt());
    }
}
