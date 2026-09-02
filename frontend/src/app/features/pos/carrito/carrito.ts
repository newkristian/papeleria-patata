import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ClienteResumen } from '../../../core/models/cliente.model';
import { CarritoService } from '../../../core/services/carrito.service';
import { ClienteService } from '../../../core/services/cliente.service';

@Component({
  selector: 'app-carrito',
  imports: [DecimalPipe],
  templateUrl: './carrito.html',
})
export class CarritoComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);

  readonly carrito = inject(CarritoService);

  readonly clientes = signal<ClienteResumen[]>([]);
  readonly cargandoClientes = signal(false);
  readonly errorClientes = signal<string | null>(null);

  ngOnInit(): void {
    this.cargandoClientes.set(true);
    this.clienteService.listar().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.cargandoClientes.set(false);
      },
      error: () => {
        this.errorClientes.set('No se pudo cargar la lista de clientes.');
        this.cargandoClientes.set(false);
      },
    });
  }

  onClienteChange(valor: string): void {
    if (valor === 'mostrador') {
      this.carrito.seleccionarCliente(null);
      return;
    }
    const cliente = this.clientes().find((c) => c.id === Number(valor)) ?? null;
    this.carrito.seleccionarCliente(cliente);
  }

  onCantidadInput(productoId: number, valor: string): void {
    const cantidad = Number(valor);
    if (!Number.isNaN(cantidad)) {
      this.carrito.establecerCantidad(productoId, cantidad);
    }
  }
}
