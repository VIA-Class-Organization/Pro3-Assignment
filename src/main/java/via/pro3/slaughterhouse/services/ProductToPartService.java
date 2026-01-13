package via.pro3.slaughterhouse.services;

import com.slaughterhouse.grpc.*;
import com.slaughterhouse.grpc.Error;
import org.lognet.springboot.grpc.GRpcService;
import via.pro3.slaughterhouse.model.ProductToPart;
import via.pro3.slaughterhouse.repositories.ProductToPartRepository;

import java.util.List;
import java.util.stream.Collectors;
@GRpcService
public class ProductToPartService extends ProductToPartServiceGrpc.ProductToPartServiceImplBase
{

  private final ProductToPartRepository productToPartRepository;

  public ProductToPartService(ProductToPartRepository productToPartRepository)
  {
    this.productToPartRepository = productToPartRepository;
  }

  public CreateProductToPartResponse createProductToPart(
      CreateProductToPartRequest request)
  {
    ProductToPart entity = new ProductToPart();
    entity.setProductId(request.getMapping().getProductId());
    entity.setAnimalPartId(request.getMapping().getPartId());
    ;
    entity.setQuantity(request.getMapping().getQuantity());

    productToPartRepository.save(entity);

    ProductToPartProto protoMapping = ProductToPartProto.newBuilder().setId(entity.getId()).setProductId(entity.getProductId())
        .setPartId(entity.getPartId()).setQuantity(entity.getQuantity()).build();

    return CreateProductToPartResponse.newBuilder().setMapping(protoMapping).build();
  }

  public ListProductToPartResponse listProductToPart()
  {
    List<ProductToPartProto> mappings = productToPartRepository.findAll().stream().map(
        entity -> ProductToPartProto.newBuilder().setId(entity.getId()).setProductId(entity.getProductId())
            .setPartId(entity.getPartId()).setQuantity(entity.getQuantity()).build()).collect(Collectors.toList());

    return ListProductToPartResponse.newBuilder().addAllMappings(mappings).build();
  }

  public ProductToPartProto getProductToPart(int id)
  {
    return productToPartRepository.findById(id).map(
        entity -> ProductToPartProto.newBuilder().setId(entity.getId()).setProductId(entity.getProductId()).setPartId(entity.getPartId())
            .setQuantity(entity.getQuantity()).build()).orElse(null);
  }

  public DeleteResponse deleteProductToPart(int id)
  {
    DeleteResponse.Builder response = DeleteResponse.newBuilder();
    if (productToPartRepository.existsById(id))
    {
      productToPartRepository.deleteById(id);
    }
    else
    {
      response.setError(Error.newBuilder().setCode(404).setMessage("ProductToPart mapping not found").build());
    }
    return response.build();
  }
}