package via.pro3.slaughterhouse.services;

import com.google.protobuf.Timestamp;
import com.slaughterhouse.grpc.*;
import org.lognet.springboot.grpc.GRpcService;
import via.pro3.slaughterhouse.model.Animal;
import via.pro3.slaughterhouse.repositories.AnimalRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@GRpcService public class AnimalService
    extends AnimalServiceGrpc.AnimalServiceImplBase
{

  private final AnimalRepository animalRepository;

  public AnimalService(AnimalRepository animalRepository)
  {
    this.animalRepository = animalRepository;
  }

  public CreateAnimalResponse createAnimal(CreateAnimalRequest request)
  {
    Animal entity = new Animal();
    entity.setRegistrationNumber(request.getAnimal().getRegistrationNumber());
    entity.setWeight(request.getAnimal().getWeight());
    entity.setArrivalTime(LocalDateTime.now());
    entity.setOrigin(request.getAnimal().getOrigin());

    animalRepository.save(entity);

    AnimalProto protoAnimal = AnimalProto.newBuilder()
        .setId(entity.getId())  // Long maps to proto int64
        .setRegistrationNumber(entity.getRegistrationNumber())
        .setWeight(entity.getWeight()).setArrivalTime(Timestamp.newBuilder()
            .setSeconds(entity.getArrivalTime().atZone(ZoneId.systemDefault())
                .toEpochSecond()).build()).setOrigin(entity.getOrigin())
        .build();

    return CreateAnimalResponse.newBuilder().setAnimal(protoAnimal).build();
  }

  public ListAnimalsResponse listAnimals()
  {
    List<AnimalProto> animals = animalRepository.findAll().stream().map(
        entity -> AnimalProto.newBuilder().setId(entity.getId())
            .setRegistrationNumber(entity.getRegistrationNumber())
            .setOrigin(entity.getOrigin()).setWeight(entity.getWeight())
            .setArrivalTime(Timestamp.newBuilder().setSeconds(
                entity.getArrivalTime().atZone(ZoneId.systemDefault())
                    .toEpochSecond()).build()).setOrigin(entity.getOrigin())
            .build()).collect(Collectors.toList());

    return ListAnimalsResponse.newBuilder().addAllAnimals(animals).build();
  }

  public AnimalProto getAnimal(int id)
  {
    return animalRepository.findById(id).map(
        entity -> AnimalProto.newBuilder().setId(entity.getId())
            .setRegistrationNumber(entity.getRegistrationNumber())
            .setWeight(entity.getWeight()).setArrivalTime(Timestamp.newBuilder()
                .setSeconds(
                    entity.getArrivalTime().atZone(ZoneId.systemDefault())
                        .toEpochSecond()).build()).setOrigin(entity.getOrigin())
            .build()).orElse(null);
  }

  public DeleteResponse deleteAnimal(int id)
  {
    DeleteResponse.Builder response = DeleteResponse.newBuilder();
    if (animalRepository.existsById(id))
    {
      animalRepository.deleteById(id);
    }
    else
    {
      response.setError(com.slaughterhouse.grpc.Error.newBuilder().setCode(404)
          .setMessage("Animal not found").build());
    }
    return response.build();
  }
}
