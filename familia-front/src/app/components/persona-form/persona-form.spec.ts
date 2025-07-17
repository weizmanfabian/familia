import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonaForm } from './persona-form';

describe('PersonaForm', () => {
  let component: PersonaForm;
  let fixture: ComponentFixture<PersonaForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonaForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
