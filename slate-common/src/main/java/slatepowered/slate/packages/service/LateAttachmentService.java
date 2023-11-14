package slatepowered.slate.packages.service;

import slatepowered.reco.rpc.RemoteAPI;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.service.Service;
import slatepowered.slate.service.ServiceKey;
import slatepowered.slate.service.remote.RemoteServiceKey;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides a way for the master to load packages on a node
 * after it has been initialized.
 */
public interface LateAttachmentService extends Service, RemoteAPI {

    ServiceKey<LateAttachmentService> KEY = ServiceKey.local(LateAttachmentService.class);

    static RemoteServiceKey<LateAttachmentService> remote() {
        return RemoteServiceKey.remote(LateAttachmentService.class);
    }

    /**
     * Try and attach the given package attachments immediately.
     *
     * @param attachments The attachments.
     */
    void attachImmediate(List<PackageAttachment<?>> attachments);

    CompletableFuture<Void> attachImmediateAsync(List<PackageAttachment<?>> attachments);

}
