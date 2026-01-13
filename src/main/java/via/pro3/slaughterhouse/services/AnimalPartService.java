package via.pro3.slaughterhouse.services;

import com.slaughterhouse.grpc.AnimalPartServiceGrpc.AnimalPartServiceImplBase;
import com.slaughterhouse.grpc.CreateAnimalPartRequest;
import com.slaughterhouse.grpc.CreateAnimalPartResponse;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import via.pro3.slaughterhouse.model.AnimalPart;
import via.pro3.slaughterhouse.repositories.AnimalPartRepository;
import com.slaughterhouse.grpc.*;
@GRpcService public class AnimalPartService
    extends AnimalPartServiceImplBase
{
  private final AnimalPartRepository partRepository;

  public AnimalPartService(AnimalPartRepository partRepository)
  {
    this.partRepository = partRepository;
  }

  @Override public void createAnimalPart(CreateAnimalPartRequest request,
      StreamObserver<CreateAnimalPartResponse> responseObserver)//needed to send grpc services
  {
    // create part
    var animalPart = new AnimalPart();
    animalPart.setId(request.getPart().getAnimalId());
    animalPart.setAnimalId(request.getPart().getAnimalId());
    animalPart.setPartType(request.getPart().getPartType());
    animalPart.setWeight(request.getPart().getWeight());
    // save part
    partRepository.save(animalPart);
    // create animalPart
    var protoAnimalPart = AnimalPartProto.newBuilder().setId(animalPart.getId())
        .setAnimalId(animalPart.getAnimalId())
        .setPartType(animalPart.getPartType()).setWeight(animalPart.getWeight())
        .build();
    //add it to the response
    var response = CreateAnimalPartResponse.newBuilder()
        .setPart(protoAnimalPart).build();
    //stream response
    responseObserver.onNext(response);// needed for grpc services
    responseObserver.onCompleted();//send grpc
  }

  @Override public void listAnimalParts(com.google.protobuf.Empty request,
      StreamObserver<ListAnimalPartsResponse> responseObserver)
  {
    //Get parts
    var animalParts = partRepository.findAll().stream().map(
        animalPart -> AnimalPartProto.newBuilder().setId(animalPart.getId())
            .setAnimalId(animalPart.getAnimalId())
            .setWeight(animalPart.getWeight())
            .setPartType(animalPart.getPartType()).build()).toList();
    // create response
    var response = ListAnimalPartsResponse.newBuilder().addAllParts(animalParts)
        .build();
    // stream response
    responseObserver.onNext(response);// needed for grpc services
    responseObserver.onCompleted();//send grpc
  }

  @Override public void getAnimalPart(GetByIdRequest idRequest,
      StreamObserver<AnimalPartProto> responseObserver)
  {
    //get animal part
    var animalPart = partRepository.findById(idRequest.getId()).orElse(null);
    //if null stream exception
    if (animalPart == null)
    {
      responseObserver.onError(
          io.grpc.Status.NOT_FOUND
              .withDescription("AnimalPart with ID " + idRequest.getId() + " not found")
              .asRuntimeException()
      );
      return; //exit
    }
    //create response
    var response = AnimalPartProto.newBuilder()
        .setAnimalId(animalPart.getAnimalId())
        .setPartType(animalPart.getPartType())
        .setId(animalPart.getId())
        .setWeight(animalPart.getWeight())
        .build();
    //stream response
    responseObserver.onNext(response);// needed for grpc services
    responseObserver.onCompleted();//send grpc
  }
  @Override
  public void deleteAnimalPart(DeleteByIdRequest id,
      StreamObserver<DeleteResponse> responseObserver)
  {
    //check if the part exists
    if (!partRepository.existsById(id.getId()))
    {
      responseObserver.onError(
          io.grpc.Status.NOT_FOUND
              .withDescription("AnimalPart with ID " + id.getId() + " not found")
              .asRuntimeException()
      );
      return; //exit
    }
    //delete
    partRepository.deleteById(id.getId());
    var response = DeleteResponse.newBuilder().build();
    // send response
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}